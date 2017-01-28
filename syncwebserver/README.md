#Readme

This is a simple java server application. The api details are outlined in [#endpoints](#endpoints).

To run the server, execute the jar file using

*windows*

``

*linux/osx*

``

Run in background by calling ``

##Server options

|Option|Description|
|---|---|
|`-v`/`--verbose`|Sets logging to be enabled|
|`-p`/`--path <path>`|Sets the output path for uploaded files|
|`--port <port>`|Sets the port of the server to run on|

##Endpoints

###/plants/ `post`

Request body will be stored as it is sent to the specified `--path`. Expected JSON encoded (or binary blob) of plant list as defined in the GrowTracker app.

Binary blob (encrypted) files will be stored as encrypted. Run server with `--decrypt --password "password"` to decrypt. Files will be decrypted in original file paths.

###/image/ `post`

Send an image to be stored with the following post body

|Key|Value|
|---|---|
|`image`|Image data to be uploaded|
|`filename`|Path to store image relative to specified `--path`|

###Response

You will receive a `{"success": true|false}` response with either a `400` or `200` response code
