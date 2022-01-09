from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import cgi
import os
import pathlib

class Server(BaseHTTPRequestHandler):
    def _set_headers(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        
    def do_HEAD(self):
        self._set_headers()
        
    def do_GET_global(self):
        self._set_headers()
        with open('data.json') as json_file:
            data = json.load(json_file)
            self.wfile.write(json.dumps(data).encode())

    def do_GET_object(self, id):
        self._set_headers()
        with open('data.json') as json_file:
            data = json.load(json_file)
            self.wfile.write(json.dumps(data[id]).encode())

    def do_GET_objectinfo(self, path):
        id, info = path.split('/', 1)
        self._set_headers()
        with open('data.json') as json_file:
            data = json.load(json_file)
            self.wfile.write(json.dumps(data[id][info]).encode())
        
    def do_GET_image(self, path):
        id, image = path.split('/', 1)
        path_dir = pathlib.Path(__file__).parent.resolve()
        path_to_image = str(path_dir) + '/data/'+str(id)+'/'+str(image)
        statinfo = os.stat(path_to_image)
        img_size = statinfo.st_size
        self.send_response(200)
        self.send_header("Content-type", "image/png")
        self.send_header("Content-length", img_size)
        self.end_headers() 
        with open(path_to_image, 'rb') as image:
            self.wfile.write(image.read())

    # GET sends back a Hello world message
    def do_GET(self):
        # Automatically populate data.json if new subdirectories appear
        path_dir = pathlib.Path(__file__).parent.resolve()
        lenImages = len(next(os.walk(str(path_dir)+'/data'))[1])
        with open('data.json', 'r+') as jsonFile:
            data = json.loads(jsonFile.read())
            lenJson = len(data)
            if lenImages > lenJson:
                for i in range(lenImages-lenJson):
                    with open('data/'+str(lenJson+i)+'/info.txt', 'r') as jsonPlus:
                        dataPlus = {str(lenJson+i): json.loads(jsonPlus.read())}
                        data.update(dataPlus)
            jsonFile.seek(0)
            json.dump(data, jsonFile)
            jsonFile.truncate()

        myPath = self.path[1:]
        if myPath == '':
            self.do_GET_global()
        elif myPath.isdigit():
            self.do_GET_object(myPath)
        elif '.png' in myPath:
            self.do_GET_image(myPath)
        elif '/' in myPath:
            self.do_GET_objectinfo(myPath)
        
    def do_PUT(self):
        length = int(self.headers.get('content-length'))
        message = json.loads(self.rfile.read(length))
        myPath = self.path[1:]
        try:
            with open('data.json', 'r+') as jsonFile:
                data = json.loads(jsonFile.read())
                data[myPath]["status"] = message["status"]
                jsonFile.seek(0)
                json.dump(data, jsonFile)
                jsonFile.truncate()

            # send the message back
            self._set_headers()
            self.wfile.write(json.dumps(data).encode())
        except:
            print('error')
        
    # POST echoes the message adding a JSON field
    def do_POST(self):
        ctype, pdict = cgi.parse_header(self.headers.get('content-type'))
        
        # refuse to receive non-json content
        if ctype != 'application/json':
            self.send_response(400)
            self.end_headers()
            return
            
        # read the message and convert it into a python dictionary
        length = int(self.headers.get('content-length'))
        message = json.loads(self.rfile.read(length))
        
        # add a property to the object, just to mess with data
        # message['received'] = 'ok'
        
        try:
            with open('data.json', 'r+') as jsonFile:
                data = json.loads(jsonFile.read())
                lenJson = len(data)
                data[lenJson] = message
                jsonFile.seek(0)
                json.dump(data, jsonFile)
                jsonFile.truncate()

            # send the message back
            self._set_headers()
            self.wfile.write(json.dumps(data).encode())
        except:
            print('error')


        
def run(server_class=HTTPServer, handler_class=Server, port=8008):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    
    print("Starting httpd on port %d..." % port)
    httpd.serve_forever()
    
if __name__ == "__main__":
    from sys import argv
    
    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()