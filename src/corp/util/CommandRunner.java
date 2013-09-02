package corp.util;

public class CommandRunner {

	/**
	 * See http://stackoverflow.com/questions/7200307/execute-unix-system-command-from-java-problem
	 * @param cmd
	 */
	public static boolean run(String cmd){
        try {  
        	String[] cmds = constructCommandsBySystem(cmd);
        	String[] env = constructEnvironmentBySystem();
        	Process p = null; 
        	if (env == null)
        		p = Runtime.getRuntime().exec(cmds);
        	else
        		p = Runtime.getRuntime().exec(cmds, env);
       
            return (p.waitFor() == 0);
        } catch (Exception e) {  
            e.printStackTrace();  
            return false;
        }
	}
	
	private static String[] constructCommandsBySystem(String cmd) {
		if (System.getProperty("os.name").contains("Windows"))
			return new String[]{"C:\\cygwin\\bin\\bash.exe", "-c", cmd};
		else 
			return new String[]{"/bin/bash", "-c", cmd};
			
	}
	
	private static String[] constructEnvironmentBySystem() {
		if (System.getProperty("os.name").contains("Windows"))
			return new String[]{"PATH=%PATH%;C:/cygwin/bin"};
		else
			return null;
	}
}