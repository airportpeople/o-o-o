// Engine_FM1
// this engine is based entirely off of Eli Fieldsteel's
// beautifully succinct FM synth: https://sccode.org/1-5bA
Engine_FM1 : CroneEngine {
	// <FM1>
	var fm1Bus;
	var fm1Syn;
	// </FM1>
	
	
	*new { arg context, doneCallback;
		^super.new(context, doneCallback);
	}
	
	alloc {
		
		// <FM1>
		
		// initialize synth defs
		SynthDef("FM1", {
			arg freq=500, mRatio=1, cRatio=1,
			index=1, iScale=5, cAtk=4, cRel=(-4),
			amp=0.2, atk=0.01, rel=3, pan=0,
			out=0, fx=0, fxsend=(-25);
			var car, mod, env, iEnv;
			
			//index of modulation
			iEnv = EnvGen.kr(
				Env(
					[index, index*iScale, index],
					[atk, rel],
					[cAtk, cRel]
				)
			);
			
			//amplitude envelope
			env = EnvGen.kr(Env.perc(atk,rel,curve:[cAtk,cRel]),doneAction:2);
			
			//modulator/carrier
			mod = SinOsc.ar(freq * mRatio, mul:freq * mRatio * iEnv);
			car = SinOsc.ar(freq * cRatio + mod) * env * amp;
			
			car = Pan2.ar(car, pan)/8;

			//direct out/reverb send
			Out.ar(out, car);
			Out.ar(fx, car * fxsend.dbamp);
		}).add;
		
		//reverb
		SynthDef("FM1FX", {
			arg in=0, out=0, dec=4, lpf=1500;
			var sig;
			sig = In.ar(in, 2).sum;
			sig = DelayN.ar(sig, 0.03, 0.03);
			sig = CombN.ar(sig, 0.1, {Rand(0.01,0.099)}!32, dec);
			sig = SplayAz.ar(2, sig);
			sig = LPF.ar(sig, lpf);
			5.do{sig = AllpassN.ar(sig, 0.1, {Rand(0.01,0.099)}!2, 3)};
			sig = LPF.ar(sig, lpf);
			sig = LeakDC.ar(sig);
			Out.ar(out, sig.poll);
		}).add;
		
		// initialize fx synth and bus
		context.server.sync;
		fm1Bus = Bus.audio(context.server,2);
		context.server.sync;
		fm1Syn=Synth("FM1FX",[\in,fm1Bus],context.server);
		context.server.sync;
		
		this.addCommand("fm1","ffffffffffff",{ arg msg;
			Synth.before(fm1Syn,"FM1",[
				\freq,msg[1],
				\amp,msg[2],
				\pan,msg[3],
				\atk,msg[4],
				\rel,msg[5],
				\cAtk,msg[6],
				\cRel,msg[7],
				\mRatio,msg[8],
				\cRatio,msg[9],
				\index,msg[10],
				\iScale,msg[11],
				\fxsend,msg[12],
				\out,0,
				\fx,fm1Bus,
			]);
		});
		// </FM1>
	}
	
	
	free {
		// <FM1>
		fm1Bus.free;
		fm1Syn.free;
		// </FM1>
	}
}

