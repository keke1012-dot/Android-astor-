"""Original Astor entrance cue: synthetic pads, bells and arpeggio, no samples."""
from pathlib import Path
import wave
import numpy as np

RATE = 44100
DURATION = 7.8
t = np.arange(round(RATE * DURATION)) / RATE
mix = np.zeros((len(t), 2), dtype=np.float64)

def tone(midi, start, duration, gain, pan=0, bell=False):
    a = int(start * RATE)
    b = min(len(t), a + int(duration * RATE))
    x = np.arange(b-a) / RATE
    f = 440 * 2**((midi-69)/12)
    if bell:
        signal = (np.sin(2*np.pi*f*x) * np.exp(-x*1.7)
                  + .28*np.sin(2*np.pi*f*2.01*x)*np.exp(-x*3.2)
                  + .09*np.sin(2*np.pi*f*3*x)*np.exp(-x*4))
        env = np.minimum(x/.012, 1) * np.minimum((duration-x)/.4, 1)
    else:
        signal = (np.sin(2*np.pi*f*x + .025*np.sin(2*np.pi*3*x))
                  + .24*np.sin(2*np.pi*f*1.003*x)
                  + .10*np.sin(2*np.pi*f*2*x))
        env = np.minimum(x/.7, 1)*np.minimum((duration-x)/1.4, 1)
    v = signal * np.clip(env, 0, 1) * gain
    mix[a:b, 0] += v * np.sqrt((1-pan)/2)
    mix[a:b, 1] += v * np.sqrt((1+pan)/2)

# E minor add9, then an airy Cmaj7 colour; an original ascending six-note motif.
for note in [40, 52, 59, 64, 66]:
    tone(note, 0, 7.7, .035 if note < 60 else .025)
for note in [48, 55, 59, 64]:
    tone(note, 3.4, 4.3, .023, .15)
for n, note in enumerate([64, 71, 76, 78, 83, 88]):
    tone(note, .45+n*.5, 3.2, .075 if n < 4 else .055, (-1)**n*.28, True)
tone(83, 4.2, 3.5, .045, -.15, True)
tone(90, 4.65, 3.0, .03, .2, True)
# Quiet echo, with a final fade to exact silence.
dry = mix.copy()
for delay, gain in [(.23, .16), (.47, .09), (.71, .045)]:
    d = int(delay*RATE)
    mix[d:] += dry[:-d, ::-1] * gain
mix *= np.minimum(t/.03, 1)[:, None]
mix *= np.clip((DURATION-t)/.8, 0, 1)[:, None]
mix *= .60 / max(np.max(np.abs(mix)), 1e-9)
assert np.isfinite(mix).all() and np.max(np.abs(mix)) < 1
pcm = np.round(mix*32767).astype('<i2')
target = Path(__file__).parent/'app/src/main/res/raw/astor_entree.wav'
target.parent.mkdir(parents=True, exist_ok=True)
with wave.open(str(target), 'wb') as f:
    f.setnchannels(2)
    f.setsampwidth(2)
    f.setframerate(RATE)
    f.writeframes(pcm.tobytes())
print(f'Original cue: {DURATION}s, stereo {RATE}Hz, peak {np.max(np.abs(mix)):.2f}')
