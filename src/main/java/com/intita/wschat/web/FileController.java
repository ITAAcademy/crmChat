package com.intita.wschat.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intita.wschat.config.ChatPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intita.wschat.services.ChatLangService;
import com.intita.wschat.util.Transliterator;

import utils.RandomString;

@Controller
public class FileController {

	private final static Logger log = LoggerFactory.getLogger(FileController.class);
	private final int DEFAULT_FILE_PREFIX_LENGTH = 15;
	static final private ObjectMapper mapper = new ObjectMapper();
	@Value("${crmchat.upload_dir}")
	private String uploadDir;
	@Value("${multipart.max-file-size}")
	private String MaxFileSizeString;
	
	//File Size string looks like 100Mb, so wee need check ending, remove last two symbols and convert to bytes count
	private int convertFileSizeStringToBytes(String size){
		int maxFileLength = Integer.parseInt(size.substring(0, size.length()-2));//Remove 'Mb' at end
		String ending = size.substring(size.length()-2,size.length()).toLowerCase();
		switch(ending){
		case "mb": maxFileLength *= 1000000;
		break;
		case "kb": maxFileLength *= 1000;
		break;
		}
		return maxFileLength;
	}
	@Autowired ChatLangService chatLangService;
	@RequestMapping(method = RequestMethod.POST, value = "/upload_file/{roomId}")
	@ResponseBody
	public void saveFile(MultipartHttpServletRequest request,
			HttpServletResponse response, Authentication auth,@PathVariable("roomId") Long roomId) {
		//0. notice, we have used MultipartHttpServletRequest
		int maxFileLength = convertFileSizeStringToBytes(MaxFileSizeString);
		//String contentLengthStr = request.getHeader("content-length");
		//int fileSize = Integer.parseInt(contentLengthStr);
		//1. get the files from the request object
		Iterator<String> itr =  request.getFileNames();
		ArrayList<String> downloadLinks = new ArrayList<String>();
		log.info("hasNext:"+itr.hasNext());

		if(!itr.hasNext())
		{
			try {
				response.getWriter().write("Error,"+((HashMap<String,String>)chatLangService.getLocalization().get("fileOperations")).get("fileEmpty"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		while (itr.hasNext())
		{
			MultipartFile mpf = request.getFile(itr.next());
			boolean fileIsEmpty = mpf.getSize() == 0;
			boolean fileIsTooBig= mpf.getSize() > maxFileLength;
			if(fileIsTooBig){
				try {
					response.sendError(HttpServletResponse.SC_FORBIDDEN,((HashMap<String,String>)chatLangService.getLocalization().get("fileOperations")).get("fileSizeOverflowLimit"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
			if (fileIsEmpty)
			{
				try {
					response.sendError(HttpServletResponse.SC_FORBIDDEN,((HashMap<String,String>)chatLangService.getLocalization().get("fileOperations")).get("fileEmpty"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
			ChatPrincipal chatPrincipal = (ChatPrincipal)auth.getPrincipal();
			String mainDir = ""+roomId;
			String subDir = ""+chatPrincipal.getChatUser().getId();
			String realPathtoUploads =  uploadDir+File.separator+mainDir+File.separator+subDir+File.separator;
			File dir = new File(realPathtoUploads);
			boolean exists = dir.exists();
			if(!exists)
				dir.mkdirs();
			String orgName=null;
			try {
				//just temporary save file info into ufile

				orgName = randomizeFileName(mpf.getOriginalFilename());
				String filePath = realPathtoUploads + orgName;
				File dest = new File(filePath);
				mpf.transferTo(dest);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			String hostPart = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
			String downloadLink =  hostPart+"/"+ String.format("download_file?owner_id=%1$s&room_id=%2$s&file_name=%3$s",chatPrincipal.getChatUser().getId(),roomId,orgName);
			downloadLinks.add(downloadLink);
			log.info("downloadLink:"+downloadLink);
		}
		//
		try {
			String responseStr = mapper.writeValueAsString(downloadLinks);
			byte[] byteOfResponse = responseStr.getBytes("UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentLength(byteOfResponse.length);
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(responseStr);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return downloadLink;
		//////
	}
	/**
	 * Size of a byte buffer to read/write file
	 */
	private static final int BUFFER_SIZE = 4096;

	/**
	 * Path of the file to be downloaded, relative to application's directory
	 */

	/**
	 * Method for handling file download request from client
	 */
	@RequestMapping(method = RequestMethod.GET, value="/download_file")
	public void doDownload(HttpServletRequest request,
			HttpServletResponse response,@RequestParam Long owner_id,@RequestParam Long room_id, @RequestParam String file_name) throws IOException {

		// get absolute path of the application
		ServletContext context = request.getServletContext();
		// String appPath = context.getRealPath("");
		// construct the complete absolute path of the file
		String mainDir = ""+room_id;
		String subDir = ""+owner_id;
		String fullPath = uploadDir +File.separator+mainDir+File.separator+subDir+File.separator+ file_name;      
		File downloadFile = new File(fullPath);
		if (!downloadFile.exists()){
			//response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			log.debug("No such file:"+fullPath);
			//return;
		}
		FileInputStream inputStream = null;
		inputStream = new FileInputStream(downloadFile);

		// get MIME type of the file
		String mimeType = context.getMimeType(fullPath);
		if (mimeType == null) {
			// set to binary type if MIME mapping not found
			mimeType = "application/octet-stream";
		}

		// set content attributes for the response
		response.setContentType(mimeType);
		response.setContentLength((int) downloadFile.length());

		// set headers for the response
		String headerKey = "Content-Disposition";
		
		String headerValue = String.format("attachment; filename='%s\'",
				deRandomizeFileName(Transliterator.transliterate(downloadFile.getName())));
		response.setHeader(headerKey, headerValue);

		// get output stream of the response
		OutputStream outStream = response.getOutputStream();

		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = -1;

		// write bytes read from the input stream into the output stream
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}

		inputStream.close();
		outStream.close();

	}
	@ExceptionHandler(FileNotFoundException.class)
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public ModelAndView handleException(FileNotFoundException e) {
		ModelAndView mav = new ModelAndView();
	    mav.setViewName("errorpage");
	    mav.addObject("errorName", ((HashMap<String,String>)chatLangService.getLocalization().get("fileOperations")).get("fileNotFound"));
	    mav.addObject("errorMessage", e.getMessage());
	    return mav;
	}
	
	private String randomizeFileName(String fileName){
		RandomString randString = new RandomString(DEFAULT_FILE_PREFIX_LENGTH);
		String nameSufix = randString.nextString();
		return fileName + nameSufix;
	}
	private String deRandomizeFileName(String randomizedFileName){
		return randomizedFileName.substring(0,randomizedFileName.length()-DEFAULT_FILE_PREFIX_LENGTH);
	}

}
