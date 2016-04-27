package com.intita.wschat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.intita.wschat.models.BotSequence;
import com.intita.wschat.repositories.BotSequenceRepository;

@Service
@Transactional
public class BotSequenceService {

	@Autowired
	BotSequenceRepository botSequenceRepository;
	
	public BotSequence add(BotSequence sequence){
		return botSequenceRepository.save(sequence);
	}
	
	
}
