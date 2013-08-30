package corp.util;

public class CommandRunner {

	/**
	 * See http://stackoverflow.com/questions/7200307/execute-unix-system-command-from-java-problem
	 * @param cmd
	 */
	public static void run(String cmd){
		Runtime run = Runtime.getRuntime();
		String[] cmds = new String[]{"/bin/bash", "-c", cmd};
        try {  
            run.exec(cmds);
        } catch (Exception e) {  
            e.printStackTrace();  
        }
	}
}