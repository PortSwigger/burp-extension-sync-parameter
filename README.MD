# Sync Parameter

Sync Parameter is an extension to Burp Suite that provides a sync function for CSRF token parameter.

# Usage

* It's very easy. On Sync tab, just set up Encoding and Sync rules.

  * Encoding - This is encoding.
  
  * Sync requests based on the following rules: - If this is on, Sync function is enabled.

  * Enabled - If this is checked, a rule of the record is enabled.

  * Host - Target host.

  * Name - Target request parameter name.
  
  * Name(Req) - Target request parameter name. (You DO NOT NEED to set Name(Req) when request parameter name you want to sync is the same as name attribute in response.)
  
  * Value - You do not have to set this. This value will be automatically updated when synchronized.

## Sample Configuration

If this test.com responds HTML which has input element with name attribute "csrf_token", value field is updated with the csrt_token value.

Then if requests to test.com has request parameter with with name "csrf_token", the value is updated with the value of Sync tab table record.

![Sample Configuration]
(img1.png)