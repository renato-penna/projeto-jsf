package br.com.cursojsf;


import org.junit.Test;

import br.com.dao.DaoGeneric;


public class TesteJPA {
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testeConexao() {
		DaoGeneric daoGeneric = new DaoGeneric();
		daoGeneric.getEntityManager();
	}
}
