import heartpy as hp
import numpy as np

#By Aditya Bhat

def get_bpm_metric(voltage_string, time_string):

    # Modify the "," to whatever string separator you are using
    voltage_data = np.array(list(map(int, voltage_string.split(","))))

    time_data = np.array(list(map(int, time_string.split(","))))

    # Estimates sample rate using time data
    sample_rate = hp.get_samplerate_mstimer(time_data)

    # Processes data. Window size is sensitivity towards peaks. Low window size implies more peaks detected.
    clean_data= hp.remove_baseline_wander(voltage_data, sample_rate)
    working_data, measures = hp.process(clean_data, sample_rate, windowsize=0.6)

    # Returns a list with first element as bpm and second as rmssd. Change rmssd to sdnn if you want to use that metric.
    return [measures['bpm'] , measures['rmssd']]