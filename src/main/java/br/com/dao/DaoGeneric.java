package br.com.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.com.japutil.JPAUtil;

public class DaoGeneric<E> {
	private EntityManager entityManager = JPAUtil.getEntityManager();

	public EntityManager getEntityManager() {
		return entityManager;
	}
	
	public void salvar(E entidade) {
		EntityManager entityManager = JPAUtil.getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.persist(entidade);
		transaction.commit();
		entityManager.close();
	}
	
	public E merge(E entidade) {   //salva ou atualiza
		EntityManager entityManager = JPAUtil.getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		E entidadeSalva = entityManager.merge(entidade);
		transaction.commit();
		entityManager.close();
		
		return entidadeSalva;
	}
	
	
	@SuppressWarnings("unchecked")
	public E pesquisar(E entidade) {
		Object id = JPAUtil.getPrimaryKey(entidade);
		E e =  (E) entityManager.find(entidade.getClass(), id);
		return e;
	}
	
	public E pesquisar(Long id, Class<E> entidade){
		E e = (E) entityManager.find(entidade, id);
		return e;
		
	}
	
	public List<E >lista(Class<E> entidade){
		EntityManager entityManager = JPAUtil.getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		@SuppressWarnings("unchecked")
		List<E> lista=   entityManager.createQuery(" SELECT a FROM  " + entidade.getName() + " a ORDER BY a.id ASC").getResultList();
		transaction.commit();
		entityManager.close();
		return lista;
	}
	public void deletar( E  entidade){
		EntityManager entityManager = JPAUtil.getEntityManager();
		Object temporario = entityManager.merge(entidade);
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.remove(temporario);  // duas maneiras (essa linha ou abaixo)
		//entityManager.createNativeQuery("delete from  " + entidade.getClass().getSimpleName().toLowerCase() + " where id = "  + id).executeUpdate();
		transaction.commit();
		entityManager.close();
		}
	
	public void deletearPorId( E  entidade){
		EntityManager entityManager = JPAUtil.getEntityManager();
		Object id =JPAUtil.getPrimaryKey(entidade);
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		//entityManager.remove(entidade);  // duas maneiras (essa linha ou abaixo)
		entityManager.createNativeQuery("delete from  " + entidade.getClass().getSimpleName().toLowerCase() + " where id = "  + id).executeUpdate();
		transaction.commit();
		entityManager.close();
		}
	
}
