package com.gestorbd.persistencia;

import java.util.List;

import com.gestorbd.modelo.Documento;

public interface RepositorioDocumentos {
    List<Documento> cargar();
    void guardar(List<Documento> documentos);
}