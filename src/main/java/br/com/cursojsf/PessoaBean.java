package br.com.cursojsf;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;

import br.com.dao.DaoGeneric;
import br.com.dao.DaoPessoa;
import br.com.entidades.Cidades;
import br.com.entidades.Estados;
import br.com.entidades.Pessoa;
import br.com.japutil.JPAUtil;
import br.com.repository.IDaoPessoa;
import br.com.repository.IDaoPessoaImpl;

@ViewScoped
@ManagedBean(name = "pessoaBean")
public class PessoaBean {
	private Pessoa pessoa = new Pessoa();
	private DaoGeneric<Pessoa> daoGeneric = new DaoGeneric<Pessoa>();
	private DaoPessoa  daoPessoa = new DaoPessoa();
	private List<Pessoa> pessoas = new ArrayList<Pessoa>();
	private IDaoPessoa iDaoPessoa = new IDaoPessoaImpl();
	private List<SelectItem> estados;
	private List<SelectItem> cidades;
	private Part arquivoFoto;

	public String salvar() throws IOException {
		if (arquivoFoto != null) {
			/* Processsar imagem */
			byte[] imagemByte = getByte(arquivoFoto.getInputStream());
			pessoa.setFotoIconBase64Original(imagemByte); /* Salva imagem original */

			/* transformar em bufferimage */
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagemByte));

			/* Pega o tipo da imagem */
			int type = bufferedImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : bufferedImage.getType();

			int largura = 200;
			int altura = 200;

			/* Criar a miniatura */
			BufferedImage resizedImage = new BufferedImage(altura, altura, type);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(bufferedImage, 0, 0, largura, altura, null);
			g.dispose();

			/* Escrever novamente a imagem em tamanho menor */
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String extensao = arquivoFoto.getContentType().split("\\/")[1]; /* image/png */
			ImageIO.write(resizedImage, extensao, baos);

			String miniImagem = "data:" + arquivoFoto.getContentType() + ";base64,"
					+ DatatypeConverter.printBase64Binary(baos.toByteArray());

			/* Processsar imagem */
			pessoa.setFotoIconBase64(miniImagem);
			pessoa.setExtensao(extensao);
		}
		if (pessoa.getId() == null) {
			mostrarMsg("Cadastrado com sucesso!");
		} else {
			if (pessoa.getSenha().isEmpty()) {
			Pessoa pessoaTemp = new Pessoa(); 
			pessoaTemp = daoGeneric.pesquisar(pessoa);
			pessoa.setSenha(pessoaTemp.getSenha());
			}
			mostrarMsg("Atualizado com sucesso!");
		}
		pessoa = daoGeneric.merge(pessoa);
		carregarPessoas();
		novo();
		return "";

	}

	public String novo() {
		pessoa = new Pessoa();
		return "";
	}

	public String limpar() {
		pessoa = new Pessoa();
		return "";
	}

	public String apagar() {
		daoPessoa.removerPessLancl(pessoa);
		carregarPessoas();
		mostrarMsg("Excluído com sucesso!");
		novo();
		// return "primeirapagina?faces-redirect=true";
		return "";
	}

	@SuppressWarnings("unchecked")
	public String editar() {
		if (pessoa.getCidades() != null) {
			Estados estado = pessoa.getCidades().getEstados();
			pessoa.setEstados(estado);

			List<Cidades> cidades = (List<Cidades>) JPAUtil.getEntityManager()
					.createQuery("from Cidades where estados.id= " + estado.getId()).getResultList();
			List<SelectItem> selectItemsCidades = new ArrayList<>();

			for (Cidades cidade : cidades) {
				selectItemsCidades.add(new SelectItem(cidade, cidade.getNome()));
			}
			setCidades(selectItemsCidades);
		}
		return "";
	}

	public void download() throws IOException {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String fileDownloadId = params.get("fileDownloadId");
		
		
		pessoa = daoGeneric.pesquisar(Long.parseLong(fileDownloadId),Pessoa.class);
		
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse(); 
		response.addHeader("Content-Disposition", "attachment; filename=download." + pessoa.getExtensao());
		response.setContentType("application/octet-stream");
		response.setContentLength(pessoa.getFotoIconBase64Original().length);
		response.getOutputStream().write(pessoa.getFotoIconBase64Original());
		response.getOutputStream().flush();
		FacesContext.getCurrentInstance().responseComplete();

	}
	
	private void mostrarMsg(String mensagem) {
		FacesContext context = FacesContext.getCurrentInstance();
		FacesMessage message = new FacesMessage(mensagem);
		context.addMessage(null, message);
	}

	@PostConstruct
	private void carregarPessoas() {
		pessoas = daoGeneric.lista(Pessoa.class);
	}

	public void pesquisaCep(AjaxBehaviorEvent event) {
		try {
			URL url = new URL("https://viacep.com.br/ws/" + pessoa.getCep() + "/json/");
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			String cep = "";
			StringBuilder jsonCep = new StringBuilder();
			while ((cep = br.readLine()) != null) {
				jsonCep.append(cep);
			}

			Pessoa gsonAux = new Gson().fromJson(jsonCep.toString(), Pessoa.class);

			pessoa.setLogradouro(gsonAux.getLogradouro());
			pessoa.setComplemento(gsonAux.getComplemento());
			pessoa.setBairro(gsonAux.getBairro());
			pessoa.setLocalidade(gsonAux.getLocalidade());
			pessoa.setUf(gsonAux.getUf());
			pessoa.setUnidade(gsonAux.getUnidade());
			pessoa.setIbge(gsonAux.getIbge());
			pessoa.setGia(gsonAux.getGia());

			System.out.println(gsonAux);
		} catch (Exception e) {
			e.printStackTrace();
			mostrarMsg("Erro ao consultar o CEP");
		}
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public DaoGeneric<Pessoa> getDaoGeneric() {
		return daoGeneric;
	}

	public void setDaoGeneric(DaoGeneric<Pessoa> daoGeneric) {
		this.daoGeneric = daoGeneric;
	}

	public List<Pessoa> getPessoas() {
		return pessoas;
	}

	public List<SelectItem> getCidades() {
		return cidades;
	}
	
	public void setCidades(List<SelectItem> cidades) {
		this.cidades = cidades;
	}

	public String logar() {
		Pessoa pessoaUser = null;
		if (((pessoa.getLogin() != "") && (pessoa.getSenha()) != "")) {
			pessoaUser = iDaoPessoa.consutlarUsuario(pessoa.getLogin(), pessoa.getSenha());

			if (pessoaUser != null) {
				// adicionar usuario na sessão
				FacesContext context = FacesContext.getCurrentInstance();
				ExternalContext externalContext = context.getExternalContext();
				externalContext.getSessionMap().put("usuarioLogado", pessoaUser);

				return "primeirapagina.jsf";
			}
		}
		return "index.jsf";
	}

	@SuppressWarnings("static-access")
	public String deslogar() {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		externalContext.getSessionMap().remove("usuarioLogado");

		HttpServletRequest httpServletRequest = (HttpServletRequest) context.getCurrentInstance().getExternalContext()
				.getRequest();
		httpServletRequest.getSession().invalidate();
		return "index.jsf";
	}

	public boolean permiteAcesso(String acesso) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		Pessoa pessoaUser = (Pessoa) externalContext.getSessionMap().get("usuarioLogado");

		return pessoaUser.getPerfilUser().equals(acesso);
	}

	public List<SelectItem> getEstados() {
		estados = iDaoPessoa.listaEstados();
		return estados;
	}

	@SuppressWarnings("unchecked")
	public void carregaCidades(AjaxBehaviorEvent event) {

		Estados estado = (Estados) ((HtmlSelectOneMenu) event.getSource()).getValue();

		if (estado != null) {
			pessoa.setEstados(estado);
			List<Cidades> cidades = (List<Cidades>) JPAUtil.getEntityManager()
					.createQuery("from Cidades where estados.id= " + estado.getId()).getResultList();
			List<SelectItem> selectItemsCidades = new ArrayList<>();

			for (Cidades cidade : cidades) {
				selectItemsCidades.add(new SelectItem(cidade, cidade.getNome()));
			}
			setCidades(selectItemsCidades);
		}
	}

	public Part getArquivoFoto() {
		return arquivoFoto;
	}

	public void setArquivoFoto(Part arquivoFoto) {
		this.arquivoFoto = arquivoFoto;
	}

	/* Metodo que converte inputStrem para array de bytes[] */
	public byte[] getByte(InputStream is) throws IOException {

		int len;
		int size = 1024;
		byte[] buf = null;

		if (is instanceof ByteArrayInputStream) {
			size = is.available();
			buf = new byte[size];
			len = is.read(buf, 0, size);
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			buf = new byte[size];

			while ((len = is.read(buf, 0, size)) != -1) {
				bos.write(buf, 0, len);
			}

			buf = bos.toByteArray();
		}

		return buf;

	}
}
