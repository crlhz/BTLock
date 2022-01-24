# file: rfcomm-server.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: simple demonstration of a server application that uses RFCOMM sockets
#
# $Id: rfcomm-server.py 518 2007-08-10 07:20:07Z albert $
from time import sleep

from bluetooth import *

# codes
CLOSED = "0"
OPENED = "1"
WRONG = "2"
BLOCKED = "3"
LOGGED = "4"
LOGGED_OUT = "5"
WRONG_PASSWORD = "6"

ADM_MODE = "b'mode0'"
NORM_MODE = "b'mode1'"
EXIT_COMMAND = "b'exit'"
PASSWORD = "b'testtest'"

max_attempts = 3
attempts = 0
code = "b'1234'"


# b'1234;5'
def change_params():
    global code
    global max_attempts
    data = client_sock.recv(1024)
    data = str(data)
    slice_object = slice(0, 6)
    code = data[slice_object]
    code = code + "'"
    slice_object = slice(7, 8)
    max_attempts = int(data[slice_object])
    print("Nowy kod [%s]" % code)
    print("Nowa ilosc prob [%d]" % max_attempts)
    client_sock.send(CLOSED)


while True:
    server_sock = BluetoothSocket(RFCOMM)
    server_sock.bind(("", PORT_ANY))
    server_sock.listen(1)

    port = server_sock.getsockname()[1]

    uuid = "2c4ab349-4b88-4753-9dfd-7bb4b8607530"

    advertise_service(server_sock, "SampleServer",
                      service_id=uuid,
                      service_classes=[uuid, SERIAL_PORT_CLASS],
                      profiles=[SERIAL_PORT_PROFILE],
                      #                       protocols = [ OBEX_UUID ]
                      )

    print("Waiting for connection on RFCOMM channel %d" % port)

    client_sock, client_info = server_sock.accept()
    print("Accepted connection from ", client_info)

    try:
        while True:
            data = client_sock.recv(1024)
            if str(data) == code and len(str(data)) == 7:
                print("Poprawny kod [%s]" % data)
                client_sock.send(OPENED)
                attempts = 0
                for x in range(10):
                    print("Otwarte przez: " + str(10 - x) + " s")
                    sleep(1)
                print("Zamknięte!")
                client_sock.send(CLOSED)
            elif str(data) != code and len(str(data)) == 7:
                if attempts >= (max_attempts-1):
                    client_sock.send(BLOCKED)
                    print("Zamek zablokowany")
                    sleep(20)
                    client_sock.send(CLOSED)
                    print("Zamek odblokowany")
                    attempts = 0
                else:
                    print("Zly kod [%s]" % data)
                    client_sock.send(WRONG)
                    attempts += 1
                    sleep(2)
                    client_sock.send(CLOSED)
            elif str(data) == PASSWORD and len(str(data)) > 7:
                print("Poprawne haslo")
                client_sock.send(LOGGED)
                change_params()
            elif str(data) != PASSWORD and len(str(data)) > 7:
                print("Bledne haslo [%s]" % data)
                client_sock.send(WRONG_PASSWORD)
    except IOError:
        pass

    print("disconnected")

    client_sock.close()
    server_sock.close()

