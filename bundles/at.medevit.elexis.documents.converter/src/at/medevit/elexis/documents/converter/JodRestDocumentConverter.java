package at.medevit.elexis.documents.converter;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.documents.DocumentStore;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.services.IDocumentConverter;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import io.swagger.client.ApiException;
import io.swagger.client.api.ConverterControllerApi;

@Component
public class JodRestDocumentConverter implements IDocumentConverter {
	
	@Reference
	private DocumentStore documentStore;
	
	@Override
	public Optional<File> convertToPdf(IDocument document){
		ConverterControllerApi apiInstance = new ConverterControllerApi();
		apiInstance.getApiClient().setBasePath(getAppBasePath());
		try {
			File tempFile = new File(documentStore.saveContentToTempFile(document, "topdf_",
				document.getTitle() + "." + document.getMimeType(), true));
			File converted = apiInstance.convertToUsingParamUsingPOST(tempFile, "pdf", null);
			tempFile.delete();
			return Optional.of(converted);
		} catch (ElexisException | ApiException e) {
			LoggerFactory.getLogger(getClass())
				.error("Error converting document [" + document + "]", e);
		}
		return Optional.empty();
	}
	
	private String getAppBasePath(){
		return ConfigServiceHolder.get().get("jodrestconverter/basepath", "");
	}
	
	@Override
	public boolean isAvailable(){
		String basePath = getAppBasePath();
		if(StringUtils.isNotEmpty(basePath)) {
			try {
				URI uri = new URI(basePath);
				return isServiceAvailable(uri.getHost(), uri.getPort(), 500);
			} catch (URISyntaxException e) {
				LoggerFactory.getLogger(getClass()).warn("Invalid basePath URI syntax [" + basePath + "]");
			}
		}
		return false;
	}
	
	private boolean isServiceAvailable(String serverHost, int serverPort, Integer timeoutms){
		if (serverHost != null) {
			try {
				SocketAddress endpoint = new InetSocketAddress(serverHost, serverPort);
				Socket socket = new Socket();
				socket.connect(endpoint, timeoutms);
				socket.close();
				return true;
			} catch (NumberFormatException | IOException e) {
				return false;
			}
		}
		return false;
	}
}
