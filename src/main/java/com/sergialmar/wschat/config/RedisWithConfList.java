package com.sergialmar.wschat.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer;
import redis.embedded.exceptions.EmbeddedRedisException;

public class RedisWithConfList extends RedisServer{

	RedisWithConfList(ArrayList<String> args, int port) throws IOException {
		super(port);
		File executable = RedisExecProvider.defaultProvider().get();

		ArrayList<String> ok = new ArrayList<>();
		ok.add(executable.getAbsolutePath());
		for (String string : args) {
			ok.add(string);
		}
		ok.add("--port");
		ok.add(Integer.toString(port));

		
		this.args = ok;
		System.out.println(executable.getAbsolutePath());
		System.out.println(ok.get(1));

	}

    public synchronized void start() throws EmbeddedRedisException {
    	
       /* File executable = new File(args.get(0));
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(executable.getParentFile());
        File f = new File("D:\\tttt.txt");
        File f2 = new File("D:\\tttt2.txt");
        
       // pb.redirectInput(f2);
        pb.redirectOutput(f);
        try {
			pb.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
        super.start();
        
    }
    public boolean enableConfigFile(String path)
    {
    	File f = new File(path);
    	if(f.exists())
    	{
    		args.add(1, path);
    		return true;
    	}
    	return false;
    }


}
