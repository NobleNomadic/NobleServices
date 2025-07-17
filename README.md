# NobleServer
Minimal HTTP server written in C for handling GET requests.

## Setting Up This Server
Clone this repository.
Then, in HTTPLib.c, you need to modify the function HTTPFindRequestPath at around line 30, and change the path that I have set, to the path you want your page to be displayed at.
Replace this:
```c
static char localPath[128] = "/home/nomad/.webserver/index.html";
```
With your path.

Then run the build.sh script, and you can run the binary in the build folder to start the server
