# chunkJavaServer

======================

A java based raw server sending chunk of a file to a client.

This work is created for my thesis application Multisource Video Streaming. 
Here you can find the http://www.aueb.gr/users/vsiris/publications/ss1_stream_implementation.pdf paper. 

======================

It is the server of the main application. 
The server is responsible for the following procedures. 

* `chunk` - Slice every file you give to n chunks
* `DocumentBuilderFactory` - Creates an xml file with the information needed by the client to read the file
* `ServerSocket` - After creating the file starts a serverSocket waiting connections and instructions
* `Client` - Starting client for sending the files that had been asked from the client
* `listOfFiles` - Reading the listOfFiles that client send 
* `BufferedOutputStream` - Sending the raw data to the client

This server is very "dummy" it hasn't any smart functionality. It is only to send raw data to the client that asks the file. 

Feel free to use it! 
