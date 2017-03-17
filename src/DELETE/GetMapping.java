package DELETE;

import java.io.File;

import com.cs.annotations.RequestMapping;
import com.cs.enums.HttpStatusCode;
import com.cs.http.BasicHttpRequest;
import com.cs.http.BasicHttpResponse;
import com.cs.interfaces.Config;
import com.cs.interfaces.Logger;

public class GetMapping
{
	@RequestMapping( value = "/", method = "GET" )
	public void something( BasicHttpRequest request, BasicHttpResponse response, Config config, Logger log )
	{
		response.setStatusCode( HttpStatusCode.OK );
		response.setBody( new File( config.getResourcePath() + "/index.html" ) );
	}
}