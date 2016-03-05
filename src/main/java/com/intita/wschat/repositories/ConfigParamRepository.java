package com.intita.wschat.repositories;

import org.springframework.data.repository.CrudRepository;

import com.intita.wschat.models.ConfigParam;

public interface ConfigParamRepository extends CrudRepository<ConfigParam, Long> {
ConfigParam findFirstByParam(String param);
}
