# Mock Quarkus - Uso rapido

## Requisitos

1. Java 21 instalado.
2. No depende del Maven del sistema: usa `./mvnw` incluido en el proyecto.

## Configuracion

El mock lee los fixtures desde:

- `servicio-fixtures.json`

Schema asociado (opcional para validacion):

- `servicio-fixtures.schema.json`

Esto esta configurado en:

- `src/main/resources/application.properties`

Si necesitas otro path:

```bash
./mvnw quarkus:dev -Dmock.fixtures.path=/ruta/absoluta/servicio-fixtures.json
```

## Levantar en desarrollo

```bash
cd servicio-conocimiento-mock
export JAVA_HOME=/usr/lib/jvm/jdk-21.0.6-oracle-x64
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw quarkus:dev
```

Servidor:

- `http://localhost:8080`

## Endpoints

1. `GET /health`
2. `GET /metadata`
3. `POST /consultar_normativa`
4. `POST /candidatos_de_tramite`
5. `GET /normativa_tramite/{codTramite}?vigente_a=YYYY-MM-DD`

## Pruebas rapidas

```bash
curl -s http://127.0.0.1:8080/health
```

```bash
curl -s -X POST http://127.0.0.1:8080/consultar_normativa \
  -H 'Content-Type: application/json' \
  -d '{"texto":"baja de automotor por venta","filtros":{"vigente_a":"2026-08-30","tributo":"automotor"},"k":1}'
```

```bash
curl -s -X POST http://127.0.0.1:8080/candidatos_de_tramite \
  -H 'Content-Type: application/json' \
  -d '{"texto":"necesito un libre deuda","k":2}'
```

```bash
curl -s "http://127.0.0.1:8080/normativa_tramite/127?vigente_a=2010-06-01"
```

```bash
curl -s "http://127.0.0.1:8080/normativa_tramite/127"
```

## Notas del endpoint normativa_tramite

- `codTramite` es obligatorio y debe ser entero positivo.
- `vigente_a` es opcional y, cuando coincide con un caso del fixture, se prioriza ese match.
- Si no hay datos para el codigo solicitado, responde `[]`.
- Si `codTramite` es invalido (`<= 0`), responde `400` con error JSON.

## Consultas cubiertas por los fixtures

### POST /consultar_normativa

1. N01: `texto="baja de automotor por venta"`, `filtros.vigente_a="2026-08-30"`, `filtros.tributo="automotor"`, `k=3`.
2. N02: `texto="denuncia de venta automotor"`, `filtros.vigente_a="2026-08-30"`, `filtros.tributo="automotor"`, `k=3`.
3. N03: `texto="libre deuda para tramites municipales"`, `filtros.vigente_a="2026-08-30"`, `k=3`.
4. N04: `texto="valuacion o base imponible automotor"`, `filtros.tributo="automotor"`, `k=2`.
5. N05: `texto="alicuota o tasa automotor"`, `filtros.vigente_a="2026-08-30"`, `filtros.tributo="automotor"`, `k=3`.
6. N06: `texto="ordenanza tarifaria 2009 automotor"`, `filtros.vigente_a="2010-06-01"`, `filtros.tributo="automotor"`, `k=2`.
7. N07: `texto="ordenanza tarifaria 2009 automotor"`, `filtros.vigente_a="2026-08-30"`, `filtros.tributo="automotor"`, `k=3`.
8. N08: `texto="prorroga vencimientos periodo fiscal 2026"`, `filtros.vigente_a="2026-08-30"`, `k=3`.
9. N09: `texto="vencimientos automotor 2026"`, `filtros.vigente_a="2026-08-30"`, `filtros.tributo="automotor"`, `k=3`.
10. N10: `texto="exencion inexistente de ejemplo"`, `filtros.vigente_a="2026-08-30"`, `k=3` (caso vacio: devuelve `[]`).

### POST /candidatos_de_tramite

1. T01: `texto="necesito un libre deuda"`, `k=5`.
2. T02: `texto="libre deuda automotor"`, `k=5`.
3. T03: `texto="libre deuda para presentar en expediente municipal"`, `k=5`.
4. T04: `texto="quiero dar de baja un automotor"`, `k=5`.
5. T05: `texto="vendi mi auto y no quiero seguir figurando"`, `k=5`.
6. T06: `texto="consultar deuda de patente"`, `k=5`.
7. T07: `texto="imprimir boleta automotor"`, `k=5`.
8. T08: `texto="alta de automotor"`, `k=5`.
9. T09: `texto="tramite no existente de ejemplo"`, `k=5` (caso vacio: devuelve `[]`).
10. T10: `texto="necesito hacer un tramite pero no tengo clave"`, `k=5`.

### GET /normativa_tramite/{codTramite}?vigente_a=YYYY-MM-DD

1. NT01: `codTramite=36`, `vigente_a=2026-08-30`.
2. NT02: `codTramite=86`, `vigente_a=2026-08-30`.
3. NT03: `codTramite=127`, `vigente_a=2026-08-30`.
4. NT04: `codTramite=127`, `vigente_a=2010-06-01`.
5. NT05: `codTramite=9999`, `vigente_a=2026-08-30` (caso vacio: devuelve `[]`).

## Ejecutar tests

```bash
cd servicio-conocimiento-mock
export JAVA_HOME=/usr/lib/jvm/jdk-21.0.6-oracle-x64
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw test
```