# Readme

This application connects your android mobile to arduino via bluetooth and enables to and fro communication between them. Currently, arduino sends some sensor data to android device. It however can be modified to send data other way round too. 

Arduino code file should be uploaded to the board and and connections should be made accordingly. The sensor giving analogue value should be connected to A0. Uses HC-05 for bluetooth transmission. Don't try to send at latency less than 10ms, breaks.

Arduino code is given.

Uses Native Android's BLuetooth API
