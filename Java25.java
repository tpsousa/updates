// Recursos do Java 22 ao 25.
// Rode com:  java Java25.java   (precisa de JDK 25 ou superior)
//
// Repare: este arquivo NAO tem "public class" nem "public static void main".
// Isso e o "compact source file" (JEP 512), finalizado no Java 25.

// Java 25: Module Import Declarations. Importa todos os pacotes publicos do java.base
// (java.util, java.util.stream, etc.) com uma linha so.
import module java.base;

record Pedido(String cliente, double valor) {}

// Java 25: Scoped Values, uma alternativa moderna e segura ao ThreadLocal.
static final ScopedValue<String> USUARIO = ScopedValue.newInstance();

static class Pessoa {
    final String nome;

    Pessoa(String nome) {
        this.nome = nome;
    }
}

static class Cliente extends Pessoa {
    // Java 25: Flexible Constructor Bodies.
    // Antes era PROIBIDO ter codigo antes do super(). Agora podemos validar e preparar os dados.
    Cliente(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("nome obrigatorio");
        }
        var normalizado = nome.trim().toUpperCase();
        super(normalizado);
    }
}

void main() {
    // Java 25: IO.println dispensa o System.out.println
    IO.println("=== Java 25: compact source file (sem class, sem static) ===");

    var pedidos = List.of(
            new Pedido("Ana", 100),
            new Pedido("Bruno", 250),
            new Pedido("Carla", 80)
    );

    IO.println("\n=== Java 22: Unnamed Variables e Patterns (_) ===");
    try {
        Integer.parseInt("abc");
    } catch (NumberFormatException _) { // nao preciso da variavel da excecao
        IO.println("Nao e numero (excecao ignorada com _)");
    }
    if (pedidos.getFirst() instanceof Pedido(String cliente, _)) { // nao preciso do valor
        IO.println("Primeiro cliente: " + cliente);
    }

    IO.println("\n=== Java 24: Stream Gatherers ===");
    var janelas = pedidos.stream().gather(Gatherers.windowFixed(2)).toList();
    IO.println("Janelas de 2 pedidos: " + janelas);

    var acumulado = pedidos.stream()
            .map(Pedido::valor)
            .gather(Gatherers.scan(() -> 0.0, (soma, v) -> soma + v))
            .toList();
    IO.println("Total acumulado: " + acumulado);

    IO.println("\n=== Java 25: Flexible Constructor Bodies ===");
    IO.println("Cliente criado: " + new Cliente("  thiago  ").nome);
    try {
        new Cliente("  ");
    } catch (IllegalArgumentException e) {
        IO.println("Validacao antes do super() funcionou: " + e.getMessage());
    }

    IO.println("\n=== Java 25: Scoped Values ===");
    ScopedValue.where(USUARIO, "thiago").run(() -> processar());
    IO.println("Fora do escopo, tem valor? " + USUARIO.isBound());
}

void processar() {
    IO.println("Processando como usuario: " + USUARIO.get());
}
