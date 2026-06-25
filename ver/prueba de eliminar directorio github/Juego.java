import java.util.Scanner;
import java.util.Random;

/**
 * Controlador principal de Ciudad Millonaria Java.
 * Coordina menu, turnos, reglas y reportes.
 *
 * Algoritmos de ordenamiento implementados manualmente:
 *   - Burbuja (bubble sort) : propiedades de un jugador por valor total.
 *   - Seleccion (selection sort): jugadores por patrimonio para el reporte.
 */
public class Juego {

    private Tablero tablero;
    private MazoCartas mazo;
    private RankingABB ranking;
    private Jugador[] jugadores;
    private int numJugadores;
    private boolean tableroListo;
    private boolean jugadoresListos;
    private int rondaActual;
    private static final int RONDAS_MAX = 20;
    private static final int BONO_SALIDA = 200;

    private Scanner scanner;
    private Random random;

    public Juego() {
        tablero = new Tablero();
        mazo = new MazoCartas();
        ranking = new RankingABB();
        jugadores = new Jugador[4];
        numJugadores = 0;
        tableroListo = false;
        jugadoresListos = false;
        rondaActual = 0;
        scanner = new Scanner(System.in);
        random = new Random();
        inicializarMazo();
    }

    // ---------------------------------------------------------------
    // Mazo predefinido
    // ---------------------------------------------------------------
    private void inicializarMazo() {
        mazo.agregarCarta(new Carta("Ganaste la loteria de la ciudad!", "ganar_dinero", 200));
        mazo.agregarCarta(new Carta("Paga multa por infraccion de transito.", "perder_dinero", 100));
        mazo.agregarCarta(new Carta("Avanza 2 casillas por inversion exitosa.", "avanzar", 2));
        mazo.agregarCarta(new Carta("Retrocede 1 casilla por obra bloqueada.", "retroceder", 1));
        mazo.agregarCarta(new Carta("Pierdes el turno por revision fiscal.", "perder_turno", 1));
        mazo.agregarCarta(new Carta("Bono por mejor ciudadano del mes.", "ganar_dinero", 150));
        mazo.agregarCarta(new Carta("Reparaciones inesperadas en tu propiedad.", "perder_dinero", 80));
        mazo.agregarCarta(new Carta("Avanza 3 casillas por contrato especial.", "avanzar", 3));
    }

    // ---------------------------------------------------------------
    // Menu principal
    // ---------------------------------------------------------------
    public void iniciar() {
        int opcion = -1;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opcion: ");
            switch (opcion) {
                case 1: cargarTablero();        break;
                case 2: registrarJugadores();   break;
                case 3: iniciarPartida();       break;
                case 4: verEstadoGeneral();     break;
                case 5: verRanking();           break;
                case 6: reporteFinal();         break;
                case 7: System.out.println("Hasta luego!"); break;
                default: System.out.println("Opcion invalida. Intente de nuevo.");
            }
        } while (opcion != 7);
    }

    private void mostrarMenu() {
        System.out.println("\n===== CIUDAD MILLONARIA JAVA =====");
        System.out.println("1. Cargar tablero");
        System.out.println("2. Registrar jugadores");
        System.out.println("3. Iniciar partida");
        System.out.println("4. Ver estado general");
        System.out.println("5. Ver ranking (ABB)");
        System.out.println("6. Reporte general");
        System.out.println("7. Salir");
    }

    // ---------------------------------------------------------------
    // 1. Carga del tablero desde texto CSV
    // ---------------------------------------------------------------
    private void cargarTablero() {
        System.out.println("\n--- Carga del tablero ---");
        System.out.println("Ingrese las casillas en formato: nombre,tipo,precio,alquiler,precioCasa");
        System.out.println("Escriba FIN para terminar.");
        tablero = new Tablero();

        String linea;
        while (true) {
            System.out.print("> ");
            linea = scanner.nextLine().trim();
            if (linea.equalsIgnoreCase("FIN")) break;
            if (linea.isEmpty()) continue;
            try {
                String[] partes = linea.split(",");
                if (partes.length < 5) {
                    System.out.println("Formato incorrecto. Use: nombre,tipo,precio,alquiler,precioCasa");
                    continue;
                }
                String nombre     = partes[0].trim();
                String tipo       = partes[1].trim().toLowerCase();
                int precio        = Integer.parseInt(partes[2].trim());
                int alquiler      = Integer.parseInt(partes[3].trim());
                int precioCasa    = Integer.parseInt(partes[4].trim());
                Casilla c = new Casilla(nombre, tipo, precio, alquiler, precioCasa);
                tablero.agregarCasilla(c);
                System.out.println("  Agregada: " + c.toString());
            } catch (NumberFormatException e) {
                System.out.println("Error: valores numericos invalidos en la linea.");
            }
        }

        if (tablero.getTamanio() == 0) {
            System.out.println("No se cargaron casillas. El tablero esta vacio.");
            return;
        }
        tableroListo = true;
        System.out.println("Tablero cargado con " + tablero.getTamanio() + " casillas.");
    }

    // ---------------------------------------------------------------
    // 2. Registro de jugadores
    // ---------------------------------------------------------------
    private void registrarJugadores() {
        if (!tableroListo) {
            System.out.println("Primero debe cargar el tablero (opcion 1).");
            return;
        }
        int n = 0;
        while (n < 2 || n > 4) {
            n = leerEntero("Cantidad de jugadores (2 a 4): ");
            if (n < 2 || n > 4) System.out.println("Debe ser entre 2 y 4.");
        }
        numJugadores = n;
        jugadores = new Jugador[numJugadores];

        int dineroInicial = (n == 2) ? 1500 : (n == 3) ? 1700 : 1800;

        for (int i = 0; i < numJugadores; i++) {
            System.out.print("Nombre del jugador " + (i + 1) + ": ");
            String nombre = scanner.nextLine().trim();
            if (nombre.isEmpty()) nombre = "Jugador" + (i + 1);
            jugadores[i] = new Jugador(nombre, dineroInicial);
            jugadores[i].setPosicion(tablero.getInicio());
        }
        jugadoresListos = true;
        System.out.println("Jugadores registrados. Dinero inicial: $"
                + ((numJugadores == 2) ? 1500 : (numJugadores == 3) ? 1700 : 1800) + ".");
    }

    // ---------------------------------------------------------------
    // 3. Iniciar partida
    // ---------------------------------------------------------------
    private void iniciarPartida() {
        if (!tableroListo) {
            System.out.println("Primero cargue el tablero (opcion 1).");
            return;
        }
        if (!jugadoresListos) {
            System.out.println("Primero registre los jugadores (opcion 2).");
            return;
        }
        ranking = new RankingABB();
        rondaActual = 0;

        while (!finDePartida()) {
            rondaActual++;
            System.out.println("\n========== RONDA " + rondaActual + " ==========");
            for (int i = 0; i < numJugadores; i++) {
                if (jugadores[i].isActivo()) {
                    ejecutarTurno(jugadores[i]);
                    if (jugadoresActivos() == 1) break;
                }
            }
            // Actualizar ranking al final de la ronda
            actualizarRanking();
            System.out.println("\n--- Fin de ronda " + rondaActual + " ---");
            ranking.mostrarAscendente();
        }
        reporteFinal();
    }

    private boolean finDePartida() {
        if (rondaActual >= RONDAS_MAX) return true;
        return jugadoresActivos() <= 1;
    }

    private int jugadoresActivos() {
        int count = 0;
        for (int i = 0; i < numJugadores; i++) {
            if (jugadores[i].isActivo()) count++;
        }
        return count;
    }

    // ---------------------------------------------------------------
    // Turno de un jugador
    // ---------------------------------------------------------------
    private void ejecutarTurno(Jugador jugador) {
        System.out.println("\n--- Turno de " + jugador.getNombre()
                + " | Dinero: $" + jugador.getDinero()
                + " | Casilla: " + jugador.getPosicion().getCasilla().getNombre() + " ---");

        if (!jugador.puedeJugar()) return;

        boolean dadoLanzado = false;
        boolean turnoTerminado = false;

        while (!turnoTerminado) {
            System.out.println("1. Lanzar dado  2. Ver propiedades  3. Construir casa  4. Terminar turno");
            int opcion = leerEntero("Opcion: ");
            switch (opcion) {
                case 1:
                    if (dadoLanzado) {
                        System.out.println("Ya lanzaste el dado este turno.");
                    } else {
                        lanzarDadoYMover(jugador);
                        dadoLanzado = true;
                    }
                    break;
                case 2:
                    jugador.mostrarPropiedades();
                    break;
                case 3:
                    if (!dadoLanzado) {
                        System.out.println("No puede construir casas antes de lanzar el dado.");
                    } else {
                        construirCasa(jugador);
                    }
                    break;
                case 4:
                    turnoTerminado = true;
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }

    private void lanzarDadoYMover(Jugador jugador) {
        int dado = random.nextInt(6) + 1;
        System.out.println(jugador.getNombre() + " lanzo el dado: " + dado);

        Object[] resultado = tablero.avanzar(jugador.getPosicion(), dado);
        NodoCasilla destino = (NodoCasilla) resultado[0];
        int pasoPorSalida = (int) resultado[1];

        jugador.setPosicion(destino);
        Casilla casilla = destino.getCasilla();
        System.out.println("  -> avanza a: " + casilla.getNombre());

        if (pasoPorSalida == 1 && !casilla.getTipo().equals("salida")) {
            jugador.recibirDinero(BONO_SALIDA);
            System.out.println("  Paso por Salida! Bono: $" + BONO_SALIDA
                    + " | Dinero: $" + jugador.getDinero());
        }

        aplicarEfectoCasilla(jugador, casilla);
    }

    // ---------------------------------------------------------------
    // Efectos de casillas
    // ---------------------------------------------------------------
    private void aplicarEfectoCasilla(Jugador jugador, Casilla casilla) {
        String tipo = casilla.getTipo();
        switch (tipo) {
            case "salida":
                jugador.recibirDinero(BONO_SALIDA);
                System.out.println("  Cayo en Salida! Bono: $" + BONO_SALIDA);
                break;
            case "propiedad":
                manejarPropiedad(jugador, casilla);
                break;
            case "impuesto":
                System.out.println("  Impuesto! Paga: $" + casilla.getAlquilerBase());
                jugador.pagarDinero(casilla.getAlquilerBase());
                break;
            case "premio":
                System.out.println("  Premio! Recibe: $" + casilla.getAlquilerBase());
                jugador.recibirDinero(casilla.getAlquilerBase());
                break;
            case "carcel":
                System.out.println("  Carcel! Pierde el proximo turno.");
                jugador.agregarTurnoPerdido(1);
                break;
            case "carta":
                aplicarCarta(jugador);
                break;
            default:
                System.out.println("  Casilla sin efecto especial.");
        }
    }

    private void manejarPropiedad(Jugador jugador, Casilla casilla) {
        if (casilla.getDueno() == null) {
            // Propiedad libre
            System.out.println("  Propiedad libre. Precio: $" + casilla.getPrecio()
                    + " | Alquiler base: $" + casilla.getAlquilerBase());
            if (jugador.getDinero() < casilla.getPrecio()) {
                System.out.println("  No tienes dinero suficiente para comprarla.");
                return;
            }
            System.out.print("  Desea comprarla? (si/no): ");
            String resp = scanner.nextLine().trim().toLowerCase();
            if (resp.equals("si") || resp.equals("s")) {
                jugador.pagarDinero(casilla.getPrecio());
                Propiedad prop = new Propiedad(
                        casilla.getNombre(),
                        casilla.getPrecio(),
                        casilla.getAlquilerBase(),
                        casilla.getPrecioCasa(),
                        jugador);
                casilla.setDueno(jugador);
                casilla.setPropiedad(prop);
                jugador.agregarPropiedad(prop);
                System.out.println("  " + jugador.getNombre() + " compro "
                        + casilla.getNombre() + ". Dinero restante: $" + jugador.getDinero());
            }
        } else if (casilla.getDueno() == jugador) {
            System.out.println("  Esta es tu propiedad.");
        } else {
            // Propiedad de otro jugador
            Propiedad prop = casilla.getPropiedad();
            int alquiler = (prop != null) ? prop.calcularAlquilerTotal() : casilla.getAlquilerBase();
            Jugador dueno = casilla.getDueno();
            System.out.println("  Propiedad de " + dueno.getNombre()
                    + ". Paga alquiler: $" + alquiler);
            jugador.pagarDinero(alquiler);
            if (jugador.isActivo()) {
                dueno.recibirDinero(alquiler);
            }
            System.out.println("  " + jugador.getNombre() + ": $" + jugador.getDinero()
                    + " | " + dueno.getNombre() + ": $" + dueno.getDinero());
        }
    }

    private void aplicarCarta(Jugador jugador) {
        Carta carta = mazo.tomarCarta();
        if (carta == null) {
            System.out.println("  El mazo esta vacio.");
            return;
        }
        System.out.println("  CARTA: " + carta.getDescripcion());
        switch (carta.getAccion()) {
            case "ganar_dinero":
                jugador.recibirDinero(carta.getValor());
                System.out.println("  Gana $" + carta.getValor()
                        + " | Total: $" + jugador.getDinero());
                break;
            case "perder_dinero":
                jugador.pagarDinero(carta.getValor());
                System.out.println("  Pierde $" + carta.getValor()
                        + " | Total: $" + jugador.getDinero());
                break;
            case "avanzar":
                System.out.println("  Avanza " + carta.getValor() + " casillas.");
                Object[] res = tablero.avanzar(jugador.getPosicion(), carta.getValor());
                jugador.setPosicion((NodoCasilla) res[0]);
                if ((int) res[1] == 1) {
                    jugador.recibirDinero(BONO_SALIDA);
                    System.out.println("  Bono Salida: $" + BONO_SALIDA);
                }
                aplicarEfectoCasilla(jugador, jugador.getPosicion().getCasilla());
                break;
            case "retroceder":
                System.out.println("  Retrocede " + carta.getValor() + " casillas.");
                jugador.setPosicion(tablero.retroceder(jugador.getPosicion(), carta.getValor()));
                aplicarEfectoCasilla(jugador, jugador.getPosicion().getCasilla());
                break;
            case "perder_turno":
                System.out.println("  Pierde " + carta.getValor() + " turno(s).");
                jugador.agregarTurnoPerdido(carta.getValor());
                break;
            default:
                System.out.println("  Accion desconocida en la carta.");
        }
    }

    // ---------------------------------------------------------------
    // Construccion de casas
    // ---------------------------------------------------------------
    private void construirCasa(Jugador jugador) {
        if (jugador.getPropiedades().getCantidad() == 0) {
            System.out.println("No tienes propiedades donde construir.");
            return;
        }
        System.out.print("Nombre de la propiedad donde construir: ");
        String nombre = scanner.nextLine().trim();
        Propiedad prop = jugador.getPropiedades().buscarPorNombre(nombre);
        if (prop == null) {
            System.out.println("No tienes esa propiedad.");
            return;
        }
        if (prop.getCasas().estaLlena()) {
            System.out.println("Esa propiedad ya tiene el maximo de casas.");
            return;
        }
        if (jugador.getDinero() < prop.getPrecioCasa()) {
            System.out.println("No tienes dinero suficiente. Costo: $" + prop.getPrecioCasa());
            return;
        }
        jugador.pagarDinero(prop.getPrecioCasa());
        prop.construirCasa();
        System.out.println("Casa construida en " + nombre
                + ". Casas totales: " + prop.getCasas().contarCasas()
                + " | Alquiler ahora: $" + prop.calcularAlquilerTotal());
    }

    // ---------------------------------------------------------------
    // Ranking
    // ---------------------------------------------------------------
    private void actualizarRanking() {
        ranking = new RankingABB();
        for (int i = 0; i < numJugadores; i++) {
            ranking.insertar(jugadores[i]);
        }
    }

    private void verRanking() {
        if (!jugadoresListos) {
            System.out.println("Aun no hay jugadores registrados.");
            return;
        }
        actualizarRanking();
        ranking.mostrarRanking();
        System.out.println("Preorden: ");
        ranking.mostrarPreorden();
        System.out.println("Postorden: ");
        ranking.mostrarPostorden();
    }

    // ---------------------------------------------------------------
    // 4. Ver estado general
    // ---------------------------------------------------------------
    private void verEstadoGeneral() {
        if (!jugadoresListos) {
            System.out.println("No hay jugadores registrados aun.");
            return;
        }
        System.out.println("\n===== ESTADO GENERAL =====");
        for (int i = 0; i < numJugadores; i++) {
            System.out.println(jugadores[i].toString()
                    + " | Casilla: " + jugadores[i].getPosicion().getCasilla().getNombre());
        }
        tablero.mostrarTablero();
    }

    // ---------------------------------------------------------------
    // 6. Reporte final con selection sort
    // ---------------------------------------------------------------
    private void reporteFinal() {
        if (!jugadoresListos) {
            System.out.println("No hay datos para mostrar.");
            return;
        }
        System.out.println("\n===== REPORTE FINAL =====");

        // --- Selection sort por patrimonio (de mayor a menor) ---
        Jugador[] copia = new Jugador[numJugadores];
        for (int i = 0; i < numJugadores; i++) copia[i] = jugadores[i];

        // Seleccion: encuentra el maximo en cada pasada
        for (int i = 0; i < numJugadores - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < numJugadores; j++) {
                if (copia[j].calcularPatrimonio() > copia[maxIdx].calcularPatrimonio()) {
                    maxIdx = j;
                }
            }
            if (maxIdx != i) {
                Jugador temp = copia[i];
                copia[i] = copia[maxIdx];
                copia[maxIdx] = temp;
            }
        }

        for (int i = 0; i < numJugadores; i++) {
            Jugador j = copia[i];
            System.out.println((i + 1) + ". " + j.getNombre()
                    + " | Dinero: $" + j.getDinero()
                    + " | Propiedades: " + j.getPropiedades().contarPropiedades()
                    + " | Casas: " + j.getPropiedades().contarTotalCasas()
                    + " | Patrimonio: $" + j.calcularPatrimonio()
                    + " | " + (j.isActivo() ? "Activo" : "Eliminado"));
        }

        if (!ranking.estaVacio()) {
            System.out.println();
            ranking.mostrarRanking();
        }

        // Ganador
        System.out.println("\nGanador: " + copia[0].getNombre()
                + " con patrimonio de $" + copia[0].calcularPatrimonio());
    }

    // ---------------------------------------------------------------
    // Utilidades de entrada
    // ---------------------------------------------------------------
    private int leerEntero(String mensaje) {
        System.out.print(mensaje);
        while (true) {
            try {
                String linea = scanner.nextLine().trim();
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.print("Ingrese un numero valido: ");
            }
        }
    }
}
