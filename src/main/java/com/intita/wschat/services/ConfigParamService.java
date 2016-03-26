package com.intita.wschat.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.ConfigParam;
import com.intita.wschat.repositories.ConfigParamRepository;

@Service
@Transactional
public class ConfigParamService {
	@Autowired
	ConfigParamRepository configParamRepo;
	
public ConfigParam getParam(String param){
	return configParamRepo.findFirstByParam(param);
}
	public List<ConfigParam> getParams(){
		return (List<ConfigParam>) configParamRepo.findAll();
	}
}
