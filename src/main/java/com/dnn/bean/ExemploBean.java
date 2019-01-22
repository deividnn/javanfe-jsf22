/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dnn.bean;

import br.com.samuelweb.certificado.Certificado;
import br.com.samuelweb.certificado.CertificadoService;
import br.com.samuelweb.certificado.exception.CertificadoException;
import br.com.samuelweb.nfe.Nfe;
import br.com.samuelweb.nfe.dom.ConfiguracoesIniciaisNfe;
import br.com.samuelweb.nfe.exception.NfeException;
import br.com.samuelweb.nfe.util.ConstantesUtil;
import br.com.samuelweb.nfe.util.Estados;
import br.inf.portalfiscal.nfe.schema_4.retConsStatServ.TRetConsStatServ;
import java.io.File;
import java.io.Serializable;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author deivid
 */
@ManagedBean
@ViewScoped
public class ExemploBean implements Serializable {

    private String uf;
    private String ambiente;
    private String tipoCertificado;
    private String senhaCertificado;
    private String schemas;
    private String caminhoA1;
    private Properties prop;

    @PostConstruct
    public void init() {
        try {

            File conf = new File(FacesContext.getCurrentInstance().
                    getExternalContext().getRealPath("/WEB-INF/") + File.separator + "conf.properties");
            if (!conf.exists()) {
                conf.createNewFile();
            }

            prop = new Properties();
            prop.load(FileUtils.openInputStream(conf));
            this.uf = prop.getProperty("uf");
            this.tipoCertificado = prop.getProperty("tipoCerticado");
            this.ambiente = prop.getProperty("ambiente");
            this.senhaCertificado = prop.getProperty("senhaCerticado");

            File pastaSchemas = new File(FacesContext.getCurrentInstance().
                    getExternalContext().getRealPath("/WEB-INF/") + File.separator + "schemas");
            this.schemas = pastaSchemas.getAbsolutePath();

            File a1 = new File(FacesContext.getCurrentInstance().
                    getExternalContext().getRealPath("/WEB-INF/") + File.separator + "a1.pfx");
            if (a1.exists()) {
                this.caminhoA1 = a1.getAbsolutePath();
            }
        } catch (Exception e) {
            criarMensagemErro(e.toString());
            e.printStackTrace();
        }

    }

    public void statusServico() {
        try {
            salvarConfiguracoes();
            iniciaConfigurações();
            TRetConsStatServ retorno = Nfe.statusServico(ConstantesUtil.NFE);

            criarMensagem("Status:" + retorno.getCStat() + "\n");
            criarMensagem("Motivo:" + retorno.getXMotivo() + "\n");
            criarMensagem("Data:" + retorno.getDhRecbto() + "\n");
            criarMensagem("UF:" + retorno.getCUF() + "\n");
            criarMensagem("Ambiente:" + retorno.getTpAmb() + "\n");

        } catch (Exception e) {
            criarMensagemErro("erro no status serviço:" + e.toString());
            e.printStackTrace();
        }
    }

    public void salvarConfiguracoes() {
        try {

            File conf = new File(FacesContext.getCurrentInstance().
                    getExternalContext().getRealPath("/WEB-INF/") + File.separator + "conf.properties");
            if (!conf.exists()) {
                conf.createNewFile();
            }

            prop.setProperty("uf", uf);
            prop.setProperty("senhaCertificado", senhaCertificado);
            prop.setProperty("ambiente", ambiente);
            prop.setProperty("tipoCertificado", tipoCertificado);
            prop.store(FileUtils.openOutputStream(conf), null);
        } catch (Exception e) {
            criarMensagemErro(e.toString());
            e.printStackTrace();
        }
    }

    public void recebepfx(FileUploadEvent event) {
        try {
            byte[] bytes = IOUtils.toByteArray(event.getFile().getInputstream());
            File a1 = new File(FacesContext.getCurrentInstance().
                    getExternalContext().getRealPath("/WEB-INF/") + File.separator + "a1.pfx");
            if (a1.exists()) {
                a1.createNewFile();
            }
            FileUtils.writeByteArrayToFile(a1, bytes);
            caminhoA1 = a1.getAbsolutePath();
        } catch (Exception e) {
            criarMensagemErro("erro ao envia certificado a1:" + e.toString());
            e.printStackTrace();
        }
    }

    public ConfiguracoesIniciaisNfe iniciaConfigurações() throws NfeException, CertificadoException {
        Certificado certificado = certifidoA1Pfx();

        return ConfiguracoesIniciaisNfe.iniciaConfiguracoes(
                Estados.valueOf(this.uf.toUpperCase()),
                ConstantesUtil.AMBIENTE.HOMOLOGACAO,
                certificado,
                this.schemas);
    }

    private Certificado certifidoA1Pfx() throws CertificadoException {
        String caminhoCertificado = this.caminhoA1;
        String senha = this.senhaCertificado;

        return CertificadoService.certificadoPfx(caminhoCertificado, senha);
    }

    public static void criarMensagem(String texto) {
        FacesMessage mesagem = new FacesMessage(texto);
        FacesContext.getCurrentInstance().addMessage(texto, mesagem);
    }

    public static void criarMensagemErro(String texto) {
        FacesMessage mesagem = new FacesMessage(FacesMessage.SEVERITY_ERROR, texto,
                texto);
        FacesContext.getCurrentInstance().addMessage(texto, mesagem);
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(String ambiente) {
        this.ambiente = ambiente;
    }

    public String getTipoCertificado() {
        return tipoCertificado;
    }

    public void setTipoCertificado(String tipoCertificado) {
        this.tipoCertificado = tipoCertificado;
    }

    public String getSenhaCertificado() {
        return senhaCertificado;
    }

    public void setSenhaCertificado(String senhaCertificado) {
        this.senhaCertificado = senhaCertificado;
    }

    public String getSchemas() {
        return schemas;
    }

    public void setSchemas(String schemas) {
        this.schemas = schemas;
    }

    public String getCaminhoA1() {
        return caminhoA1;
    }

    public void setCaminhoA1(String caminhoA1) {
        this.caminhoA1 = caminhoA1;
    }

}
