package br.com.repository;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

import br.com.entidades.Estados;
import br.com.entidades.Pessoa;
import br.com.japutil.JPAUtil;

public class IDaoPessoaImpl implements IDaoPessoa {

	@Override
	public Pessoa consutlarUsuario(String login, String senha) {
		Pessoa pessoaUser = null;
		EntityManager entityManager = JPAUtil.getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		try {
			pessoaUser = (Pessoa) entityManager
					.createQuery(
							" SELECT a FROM  Pessoa a where a.login = '" + login + "' and a.senha= '" + senha + "' and a.ativo=true")
					.getSingleResult();
		} catch (NoResultException e) {
		//	e.printStackTrace();
			return pessoaUser;
		}
		transaction.commit();
		entityManager.close();
		return pessoaUser;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SelectItem> listaEstados() {

		List<SelectItem> selectItems = new ArrayList<>();

		EntityManager entityManager = JPAUtil.getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		List<Estados> estados = entityManager.createQuery("from Estados").getResultList();
		transaction.commit();
		entityManager.close();

		for (Estados estado : estados) {
			selectItems.add(new SelectItem(estado, estado.getNome()));
		}
		return selectItems;
	}
	
}
