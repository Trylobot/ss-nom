package data.scripts;
import com.fs.starfarer.api.Global;

// shortcut to access the log file conditionally
public class _
{
	public static void L( String message )
	{
		if( Global.getSettings().isDevMode() )
		{
			Class caller_class = sun.reflect.Reflection.getCallerClass( 2 );
			Global.getLogger( caller_class ).debug( message );
		}
	}
}
