import heartpy as hp

def get_bpm_metric(voltage_list, time_list):

    # Estimates sample rate using time data
    sample_rate = hp.get_samplerate_mstimer(time_list)

    # Processes data. Window size is sensitivity towards peaks. Low window size implies more peaks detected.
    clean_data= hp.remove_baseline_wander(voltage_list, sample_rate)
    #_ is working data (anonymous currently)
    _, measures = hp.process(clean_data, sample_rate, windowsize=0.6)

    # Returns a list with first element as bpm and second as rmssd. Change rmssd to sdnn if you want to use that metric.
    return [measures['bpm'] , measures['rmssd'], 1/sample_rate]