package sp.sd.fileoperations;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.File;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;
import java.io.Serializable;

public class FileDeleteOperation extends FileOperation implements Serializable { 
	private final String includes;
	private final String excludes;
	@DataBoundConstructor 
	 public FileDeleteOperation(String includes, String excludes) { 
		this.includes = includes;
		this.excludes = excludes;
	 }

	 public String getIncludes()
	 {
		 return includes;
	 }
	 public String getExcludes()
	 {
		 return excludes;
	 }
	 
	 public boolean runOperation(AbstractBuild build, Launcher launcher, BuildListener listener) {
		 boolean result = false;
		 try
			{
			 	listener.getLogger().println("File Delete Operation:");				
				try {	
					FilePath ws = new FilePath(build.getWorkspace(),"."); 
					result = ws.act(new TargetFileCallable(listener, build.getEnvironment(listener).expand(includes), build.getEnvironment(listener).expand(excludes)));				
				}
				catch (Exception e) {
					listener.fatalError(e.getMessage());
					return false;
				}
				
			}
			catch(Exception e)
			{
				listener.fatalError(e.getMessage());
			}	
			return result;
		} 
 
	private static final class TargetFileCallable implements FileCallable<Boolean> {
		private static final long serialVersionUID = 1;
		private final BuildListener listener;
		private final String resolvedIncludes;
		private final String resolvedExcludes;
		public TargetFileCallable(BuildListener Listener, String ResolvedIncludes, String ResolvedExcludes) {
			this.listener = Listener;
			this.resolvedIncludes = ResolvedIncludes;	
			this.resolvedExcludes = ResolvedExcludes;			
		}
		@Override public Boolean invoke(File ws, VirtualChannel channel) {
			boolean result = false;
			try {
				FilePath fpWS = new FilePath(ws);
				FilePath[] resolvedFiles = fpWS.list(resolvedIncludes, resolvedExcludes);
				if(resolvedFiles.length == 0)
				{
					listener.getLogger().println("0 files found for include pattern '" + resolvedIncludes + "' and exclude pattern '" + resolvedExcludes +"'");
					result = true;
				}
				for (FilePath fp : resolvedFiles) { 
					listener.getLogger().println(fp.getRemote() + " deleting....");
					if(fp.isDirectory())
					{
						try
						{
							fp.deleteRecursive();
							result = true;
							listener.getLogger().println("Success.");
						}
						catch(RuntimeException e)
						{
							listener.fatalError(e.getMessage());
							throw e;
						}
						catch(Exception e)
						{
							listener.fatalError(e.getMessage());
							result = false;
						}
					}
					else
					{
						try
						{
							result = fp.delete();
							listener.getLogger().println("Success.");
						}
						catch(RuntimeException e)
						{
							listener.fatalError(e.getMessage());
							throw e;
						}
						catch(Exception e)
						{
							listener.fatalError(e.getMessage());
							result = false;
						}
					}
					
				}
			}
			catch(RuntimeException e)
			{
				listener.fatalError(e.getMessage());
				throw e;
			}
			catch(Exception e)
			{
				listener.fatalError(e.getMessage());
				result = false;
			}
			return result;	
		}
		
		@Override  public void checkRoles(RoleChecker checker) throws SecurityException {
                
		}		
	}
 @Extension public static class DescriptorImpl extends FileOperationDescriptor {
 public String getDisplayName() { return "File Delete"; }

 }
}