import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Tour pelos principais recursos do Java 8 ao 21.
 * Rode com:  java TourDoJava.java   (precisa de JDK 21 ou superior)
 */
public class TourDoJava {

    // ===== Java 16: RECORDS =====
    // Uma linha gera construtor, getters (id(), name()...), equals, hashCode e toString.
    record Product(Long id, String name, double price, Instant date) {}

    // ===== Java 17: SEALED INTERFACES =====
    // O compilador sabe TODOS os tipos possiveis de Resultado.
    sealed interface Resultado permits Sucesso, NaoEncontrado, ErroIntegridade {}
    record Sucesso(Product produto) implements Resultado {}
    record NaoEncontrado(Long id) implements Resultado {}
    record ErroIntegridade(String motivo) implements Resultado {}

    public static void main(String[] args) throws Exception {

        // ===== Java 10: VAR + Java 9: List.of =====
        var produtos = List.of(
                new Product(1L, "Phone", 800.0, Instant.parse("2020-03-12T10:00:00Z")),
                new Product(2L, "Smart TV", 2190.0, Instant.parse("2021-05-20T08:30:00Z")),
                new Product(3L, "Notebook", 4500.0, Instant.parse("2022-01-10T14:00:00Z"))
        );

        secao("Java 8: Lambdas, Streams e Optional");
        var nomesCaros = produtos.stream()
                .filter(p -> p.price() > 1000)
                .map(Product::name)
                .collect(Collectors.toList());
        System.out.println("Produtos acima de 1000: " + nomesCaros);

        Optional<Product> achado = produtos.stream().filter(p -> p.id() == 2L).findFirst();
        System.out.println("Produto id 2: " + achado.map(Product::name).orElse("nenhum"));

        secao("Java 16: Records (toString e equals de graca)");
        var phone = produtos.getFirst();
        System.out.println(phone);
        System.out.println("equals: " + phone.equals(new Product(1L, "Phone", 800.0, phone.date())));

        secao("Java 15: Text Blocks");
        String json = """
                {
                  "id": %d,
                  "name": "%s",
                  "price": %.2f
                }
                """.formatted(phone.id(), phone.name(), phone.price());
        System.out.print(json);

        secao("Java 14: Switch Expressions");
        for (var p : produtos) {
            System.out.println(p.name() + " -> " + faixaDePreco(p.price()));
        }

        secao("Java 16: Pattern Matching para instanceof");
        Object[] coisas = {"Spring Boot", 42, "   "};
        for (Object obj : coisas) {
            if (obj instanceof String s && !s.isBlank()) {
                System.out.println("Texto valido: " + s.toUpperCase());
            } else {
                System.out.println("Ignorado: [" + obj + "]");
            }
        }

        secao("Java 17 + 21: Sealed + Pattern Matching em switch + Record Patterns");
        List<Resultado> resultados = List.of(
                new Sucesso(produtos.get(2)),
                new Sucesso(produtos.get(0)),
                new NaoEncontrado(1000L),
                new ErroIntegridade("produto possui dependencias")
        );
        resultados.forEach(r -> System.out.println(tratar(r)));

        secao("Java 21: Sequenced Collections");
        var lista = new ArrayList<>(produtos);
        System.out.println("Primeiro: " + lista.getFirst().name());
        System.out.println("Ultimo:   " + lista.getLast().name());
        System.out.println("Invertida: " + lista.reversed().stream().map(Product::name).toList());

        secao("Java 21: Virtual Threads");
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futuros = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                int n = i;
                futuros.add(executor.submit(() -> {
                    Thread.sleep(100); // simula espera por banco ou API externa
                    return "Tarefa " + n + " rodou em virtual thread? "
                            + Thread.currentThread().isVirtual();
                }));
            }
            for (var f : futuros) {
                System.out.println(f.get());
            }
        }
    }

    // Java 14: switch como EXPRESSAO (retorna valor, sem break)
    static String faixaDePreco(double preco) {
        return switch ((int) (preco / 1000)) {
            case 0 -> "barato";
            case 1, 2 -> "medio";
            default -> "caro";
        };
    }

    // Java 21: pattern matching em switch, record patterns e guards (when).
    // Como Resultado e sealed, nao precisa de default: o compilador garante que cobrimos tudo.
    static String tratar(Resultado r) {
        return switch (r) {
            case Sucesso(Product p) when p.price() > 2000 -> "200 OK (produto caro): " + p.name();
            case Sucesso(Product p) -> "200 OK: " + p.name();
            case NaoEncontrado(Long id) -> "404 Not Found: id " + id + " nao existe";
            case ErroIntegridade(String motivo) -> "400 Bad Request: " + motivo;
        };
    }

    static void secao(String titulo) {
        System.out.println("\n=== " + titulo + " ===");
    }
}
