package com.example.auditoria.adapter.in.web;

import java.util.List;

/** Cuerpo JSON uniforme para los errores; "tipo" muestra qué excepción de dominio se produjo. */
public record ErrorRespuesta(int status, String tipo, String mensaje, List<String> detalles) {
}
