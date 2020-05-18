#!/usr/bin/env python3

from time import sleep
import socket
import numpy as np
import matplotlib.pyplot as plt
import threading

test_str = b'\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\xf8\x08\x08\x08\xf8\x00\x00\x00\x00\x00'
HOST = '0.0.0.0'
PORT = 8070
N = 5


def split_by_n(seq, n):
    while seq:
        yield seq[:n]
        seq = seq[n:]


fig = plt.figure()
ax = fig.gca(projection='3d')
volume = np.zeros((N, N, N))
colors = [1] * N*N*N


def update_clrs():
    while True:
        sleep(1)
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.bind((HOST, PORT))
            s.listen()
            conn, addr = s.accept()
            print(f'Conn from {addr}')
            with conn:
                while True:
                    data = conn.recv(1024)
                    if not data:
                        break
                    data = data[1:]
                    data = list(split_by_n(data, N))
                    print(data)
                    # colors.clear()
                    for j in range(N):
                        for i in range(N):
                            byte = data[i][j]
                            for k in range(N):
                                bit = 1 if byte & (1 << (k + (8 - N))) else 0.2
                                colors[i*N*N + k*N + j] = bit


update = threading.Thread(target=update_clrs)
update.start()

while True:
    x = np.arange(volume.shape[0])[:, None, None]
    y = np.arange(volume.shape[1])[None, :, None]
    z = np.arange(volume.shape[2])[None, None, :]
    x, y, z = np.broadcast_arrays(x, y, z)

    ax = fig.gca(projection='3d')
    ax.scatter(x.ravel(),
               y.ravel(),
               z.ravel(),
               c=colors)

    plt.draw()
    plt.pause(0.01)
