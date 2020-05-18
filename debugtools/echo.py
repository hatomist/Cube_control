#!/usr/bin/env python3

from time import sleep

import socket
HOST='0.0.0.0'
PORT=8070

while True:
    print('\nRestart\n')
    sleep(1)
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen()
        conn, addr = s.accept()
        print(f'Conn from {addr}')
        with conn:
            while True:
                data = conn.recv(1024)
                print(data, end='', flush=True)
                
                if not data:
                    break
