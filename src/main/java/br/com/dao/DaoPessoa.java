package br.com.dao;

import br.com.entidades.Pessoa;

public class DaoPessoa extends DaoGeneric<Pessoa> {

	public void removerPessLancl(Pessoa pessoa ) {
		String sqlDeleteLancamento = "delete from lancamento where usuario_id =  " + pessoa.getId();
		getEntityManager().getTransaction().begin();
		getEntityManager().createNativeQuery(sqlDeleteLancamento).executeUpdate();
		getEntityManager().getTransaction().commit();
		
		super.deletar(pessoa);
		getEntityManager().close();
		
	}
}
