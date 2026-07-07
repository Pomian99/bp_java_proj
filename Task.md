**Wprowadzenie**
Celem projektu jest stworzenie kompletnego systemu sklepu internetowego w języku Java. System powinien umożliwiać zarządzanie produktami, obsługę zamówień, interakcję z klientem, oraz umożliwiać prostą konfigurację i rozwinięcie o dodatkowe funkcje w przyszłości.

**PREREKWIZYT:**
GIT
Inicjalizacja repozytorium Git dla projektu.
PRACA W OPARCIU O GITHUB FLOW -> Utworzenie gałęzi dla różnych funkcji.
Demonstracja pull request/merge request dla nowych funkcji.

**Wymagania Biznesowe**
**Task 1**: Stworzenie klasy Product reprezentującej produkt z polami takimi jak id, nazwa, cena, ilość dostępna.

W sklepie internetowym sprzedawane będą produkty będące elektroniką:

**Typ Produktu: Komputer**

**Specyficzna Obsługa**: Implementacja funkcji umożliwiającej konfigurację specyfikacji laptopa/komputera przed dodaniem do koszyka, takiej jak wybór procesora, ilości RAM, itp.

**Typ Produktu: Smartfon**

Specyficzna Obsługa: Dodanie funkcji umożliwiającej wybór koloru, pojemności baterii, oraz dodatkowych akcesoriów przy zakupie smartfona.

**Dla pozostałych produktów, które będą sprzedawane w sklepie typem produktu będzie po prostu: Electronics i dla produktów o tym typie nie ma żadnej dodatkowej obsługi!!!**

**CEL:**
Dostosowanie obsługi różnych typów produktów pozwala na lepsze spełnienie oczekiwań klientów. Funkcje specyficzne dla każdego typu produktu mogą zwiększyć atrakcyjność sklepu i zapewnić klientom bardziej spersonalizowane doświadczenie zakupowe.

**Task 2:** Implementacja ProductManager do dodawania, usuwania, aktualizacji oraz przeglądania produktów.

2. Obsługa Koszyka Zakupowego

**Task 3**: Utworzenie klasy Cart do przechowywania produktów dodanych przez klienta.

**Task 4**: Implementacja funkcji dodawania produktów do koszyka, przeglądania koszyka oraz składania zamówienia.

1. Obsługa Zamówień
Task 5: Stworzenie klasy Order reprezentującej zamówienie z informacjami o kliencie, produktach i sumie zamówienia.

**Task 6**: Implementacja OrderProcessor do przetwarzania zamówień oraz generowania faktur.

1. Interakcja z Użytkownikiem

**Task 7**: Utworzenie interfejsu command line umożliwiającego przeglądanie produktów, dodawanie do koszyka, składanie zamówienia.

1. Zarządzanie Czasem
**Task 8:** Wykorzystanie LocalDateTime do śledzenia czasu składania zamówienia.
2. Obsługa Wyjątków
**Task 9**: Dodanie obsługi wyjątków dla przypadków takich jak brak produktu w magazynie, problem w przetwarzaniu zamówienia itp.
3. Persystencja Danych
**Task 10**: Implementacja klasy do obsługi persystencji danych, np. zapisywanie zamówień do pliku (zaawansowane) lub lista (prostsze)
4. Wątki
**Task 11:** Implementacja obsługi wielowątkowości dla równoczesnego przetwarzania kilku zamówień.
5. Dodatkowe Funkcje
**Task 12**: Dodanie funkcji zgodnie z zapotrzebowaniem, takie jak promocje, rabaty, itp.
6. Dokumentacja
**Task 13**: Dodanie dokumentacji dla kluczowych klas i metod.
7. Asynchroniczność
**Task 14**: Zastosowanie asynchroniczności w obszarze przetwarzania zamówień.
8. Synchronizacja Czasowa
**Task 15**: Wykorzystanie klas do obsługi czasu, aby unikać problemów ze strefami czasowymi.

Zakończenie
Projekt ma na celu dostarczenie kompletnego systemu sklepu internetowego, który może być rozwijany o dodatkowe funkcje w przyszłości. Wszystkie wymagania powinny być zaimplementowane zgodnie z najwyższymi standardami kodowania i testowania.