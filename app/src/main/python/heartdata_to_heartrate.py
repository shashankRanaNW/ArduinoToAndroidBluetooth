import heartpy as hp

def get_bpm(voltage_string, time_string):
    # Modify the "," to whatever string separator you are using
    voltage_data = list(map(int, voltage_string.split(",")))
    time_data = list(map(int, time_string.split(",")))

    # Estimates sample rate using time data
    sample_rate = hp.get_samplerate_mstimer(time_data)

    # Processes data
    working_data, measures = hp.process(hp.remove_baseline_wander(voltage_data, sample_rate), sample_rate)

    # Returns bpm value
    return measures['bpm']