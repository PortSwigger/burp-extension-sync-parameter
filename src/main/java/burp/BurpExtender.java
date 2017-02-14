package burp;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import assess.SyncTableModel;
import assess.SyncParameterTab;

public class BurpExtender implements IBurpExtender, IProxyListener, IHttpListener, ITab {
	private IExtensionHelpers helpers;
	private PrintWriter stdout;
	private PrintWriter stderr;

	// Extension Name
	public static final String EXTENSION_NAME = "Sync Parameter";

	// Extension Version
	public static final String VERSION_INFO = "1.1";

	// List of sync target list except Proxy.
	public static final List<Integer> SYNC_TARGET_TOOL_LIST = Arrays.asList( IBurpExtenderCallbacks.TOOL_REPEATER,
			IBurpExtenderCallbacks.TOOL_SCANNER, IBurpExtenderCallbacks.TOOL_INTRUDER,
			IBurpExtenderCallbacks.TOOL_SEQUENCER, IBurpExtenderCallbacks.TOOL_SPIDER );

	private SyncParameterTab panel;

	//
	// implement IBurpExtender
	//
	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		// set our extension name
		callbacks.setExtensionName(EXTENSION_NAME + " " + VERSION_INFO);

		// obtain our output and error streams
		stdout = new PrintWriter(callbacks.getStdout(), true);
		stderr = new PrintWriter(callbacks.getStderr(), true);

		// register ourselves as a Proxy listener
		callbacks.registerProxyListener(this);

		// register ourselves as a Proxy listener
		callbacks.registerHttpListener(this);

		try {
			// create UI
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// main panel
					panel = SyncParameterTab.getInstance();
					panel.render();
					callbacks.customizeUiComponent(panel);

					// add the custom tab to Burp's UI
					callbacks.addSuiteTab(BurpExtender.this);
				}
			});

			this.helpers = callbacks.getHelpers();			
		} catch(Exception e) {
			// if exception occurs, letting stacktrace info show on Error tab.
			writeStackTraceToErrorTab(e);
		}
	}

	/**
	 * This is implemented for request or response via Proxy and does not handle those via some tools.
	 */
	@Override
	public void processProxyMessage(boolean messageIsRequest, IInterceptedProxyMessage message) {
		try {
			IHttpRequestResponse messageInfo = message.getMessageInfo();

			// process when message is request.
			if (messageIsRequest) {
				syncRequest(messageInfo);
			}

			// process when message is request.
			if (!messageIsRequest) {
				syncResponse(messageInfo);
			}

		} catch (Exception e) {
			// if exception occurs, letting stacktrace info show on Error tab.
			writeStackTraceToErrorTab(e);
		}
	}

	/**
	 * This is implemented for tools except Proxy, because processProxyMessage can only handle request or response via Proxy.
	 */
	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		try {
			if(SYNC_TARGET_TOOL_LIST.contains(toolFlag)){
				// process when message is request.
				if (messageIsRequest) {
					syncRequest(messageInfo);
				}

				// process when message is request.
				if (!messageIsRequest) {
					syncResponse(messageInfo);
				}				
			}
		} catch (Exception e) {
			// if exception occurs, letting stacktrace info show on Error tab.
			writeStackTraceToErrorTab(e);
		}		
	}

	@Override
	public String getTabCaption() {
		return "CSRF Token Tracker";
	}

	@Override
	public Component getUiComponent() {
		return panel;
	}

	private void syncRequest(IHttpRequestResponse messageInfo) throws Exception {
		// If Sync is Off, do not Sync.
		if (!panel.isSyncOn())
			return;

		// get original request.
		byte[] oriReqBytes = messageInfo.getRequest();
		IRequestInfo oriReqInfo = helpers.analyzeRequest(oriReqBytes);

		// get original parameter list.
		List<IParameter> paraList = oriReqInfo.getParameters();

		// get TableModel of SyncTable.
		SyncTableModel tableModel = panel.getTableModel();

		// prepare variables for sync.
		String syncParaName;
		String syncParaValue;
		String syncHost;
		IParameter addParam;
		String encoding = panel.getEncoding();
		List<IParameter> addParaList = new ArrayList<>();

		// Sync with Sync Table Records.
		for (int row = 0; row < SyncTableModel.TABLE_ROW_COUNT; row++) {
			try {
				// If record is sync enabled, do sync
				if ((Boolean) tableModel.getValueAt(row, SyncTableModel.ENABLED_COLUMN_INDEX)) {
					// host check
					syncHost = (String) tableModel.getValueAt(row, SyncTableModel.HOST_COLUMN_INDEX);
					if (syncHost == null || !syncHost.equals(messageInfo.getHttpService().getHost()))
						continue;

					// get sync name and value.
					syncParaName = (String) tableModel.getValueAt(row, SyncTableModel.NAME_COLUMN_INDEX);
					syncParaValue = (String) tableModel.getValueAt(row,
							SyncTableModel.VALUE_COLUMN_INDEX) == null ? ""
									: (String) tableModel.getValueAt(row, SyncTableModel.VALUE_COLUMN_INDEX);

					// if sync parameter name is blank, continue loop.
					if (syncParaName == null || syncParaName.equals(""))
						continue;
					
					// if sync request parameter name is set, overwrite syncPara name with it.
					String syncReqParaName = (String) tableModel.getValueAt(row, SyncTableModel.NAME_REQ_COLUMN_INDEX);
					if(syncReqParaName != null && !syncReqParaName.equals("")){
						syncParaName = syncReqParaName;
					}

					// Check all parameter.
					for (IParameter para : paraList) {
						// If parameter name is same as name of sync table record, update parameter.
						//String urlDecodedName = URLDecoder.decode(para.getName(), encoding);
						if (para != null && URLDecoder.decode(para.getName(), encoding).equals(syncParaName)) {
							addParam = this.helpers.buildParameter(para.getName(),
									URLEncoder.encode(syncParaValue, encoding), para.getType());
							addParaList.add(addParam);
							break;
						}
					}
				}
			} catch (Exception e) {
				writeStackTraceToErrorTab(e);
			}
		}

		// update parameter and request
		byte[] modRequestBytes = oriReqBytes;
		for (IParameter modPara : addParaList)
			modRequestBytes = this.helpers.updateParameter(modRequestBytes, modPara);

		// update request.
		messageInfo.setRequest(modRequestBytes);
	}

	private void syncResponse(IHttpRequestResponse messageInfo) throws Exception {
		// If Sync is Off, does not Sync.
		if (!panel.isSyncOn())
			return;

		// get response.
		byte[] responseBytes = messageInfo.getResponse();
		IResponseInfo iResponse = helpers.analyzeResponse(responseBytes);

		// get body start offset
		int offset = iResponse.getBodyOffset();

		// get body
		byte[] bodyBytes = Arrays.copyOfRange(responseBytes, offset, responseBytes.length);

		// get response as String.
		String responseStr;
		try {
			responseStr = new String(bodyBytes, panel.getEncoding());
		} catch (UnsupportedEncodingException e) {
			throw e;
		}

		syncResponseInput(messageInfo.getHttpService().getHost(), responseStr);
	}

	private void syncResponseInput(String host, String responseStr) {
		// get TableModel of SyncTable
		SyncTableModel tableModel = panel.getTableModel();

		// prepare JSoup for HTML parse.
		Document document;
		Elements elements;

		// Sync target name attribute value.
		String targetNameAttr;

		// Sync Target host
		String targetHost;

		// Flag to show whether sync record is enabled.
		boolean isEnabled;

		// Loop for all sync records.
		for (int row = 0; row < SyncTableModel.TABLE_ROW_COUNT; row++) {
			try {
				isEnabled = (Boolean) tableModel.getValueAt(row, SyncTableModel.ENABLED_COLUMN_INDEX);
				targetHost = (String) tableModel.getValueAt(row, SyncTableModel.HOST_COLUMN_INDEX);

				// If sync is enabled and host matches target host, get target
				// value from response and store it in sync table.
				if (isEnabled && targetHost != null && targetHost.equals(host)) {
					document = Jsoup.parse(responseStr);
					targetNameAttr = (String) tableModel.getValueAt(row, SyncTableModel.NAME_COLUMN_INDEX);
					if (targetNameAttr == null)
						targetNameAttr = "";

					// get input element which has target name attribute.
					elements = document.select("input[name=" + targetNameAttr + "]");
					if (elements == null || elements.size() == 0)
						continue;

					// store value in sync table
					tableModel.setValueAt(elements.get(0).attr("value"), row,
							SyncTableModel.VALUE_COLUMN_INDEX);
				}
			} catch (Exception e) {
				writeStackTraceToErrorTab(e);
			}
		}
	}

	/**
	 * This method show stack trace on the Burp Extender Error Tab.
	 * @param e
	 */
	private void writeStackTraceToErrorTab(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.flush();
		String stackTrace = sw.toString();
		stderr.println(stackTrace);
	}
}