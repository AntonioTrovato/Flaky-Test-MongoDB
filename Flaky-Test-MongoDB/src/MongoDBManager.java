import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.Arrays;
import java.util.Scanner;

import static com.mongodb.client.model.Indexes.ascending;

public class MongoDBManager {

    /**
     * Il main stabilisce una connessione con il DB Mongo, recupera la collezione ml_dataset,
     * mostra il menù e gestisce le chiamate alle varie funzioni
     * @param args argomenti da linea di comando (inutilizzati)
     */
    public static void main(String[] args) {
        //creo il client
        MongoClient mongoClient = new MongoClient("localhost",27017);

        //creo delle credenziali
        MongoCredential mongoCredential;
        mongoCredential = MongoCredential.createCredential("sampleUser","FlakyTests",
                "password".toCharArray());
        System.out.println("Connesso con successo al database");

        //accedo al database
        MongoDatabase mongoDatabase = mongoClient.getDatabase("FlakyTests");
        System.out.println("Credentials ::" + mongoCredential);

        //recupero la collezione
        MongoCollection<Document> ml_datasetCollection = mongoDatabase.getCollection("ml_dataset");
        System.out.println("Connessione alla collezione ml_dataset ottenuta con successo");

        //mostro il menù
        showMenu();

        Scanner scanner = new Scanner(System.in);
        int i = scanner.nextInt();

        while (i != 0) {
            if (i == 1)
                showFlakyTests(ml_datasetCollection);
            else if (i == 2)
                showNonFlakyTests(ml_datasetCollection);
            else if (i == 3)
                showDependentFlakyTests(ml_datasetCollection);
            else if (i == 4)
                showTestLocGT100(ml_datasetCollection);
            else if(i == 5)
                showLocCboFlakyTests(ml_datasetCollection);
            else if(i == 6)
                showTestsLocAverage(ml_datasetCollection);
            else if(i == 7)
                showFlakyTestsLocAverage(ml_datasetCollection);
            else if (i == 8)
                showMaxLocs(ml_datasetCollection);
            else if (i == 9)
                insertTest(ml_datasetCollection);
            else if (i == 10)
                retrieveyNameProject(ml_datasetCollection);
            else if (i == 11)
                sortByLoc(ml_datasetCollection);
            else if (i == 12)
                makeGodClassIndex(ml_datasetCollection);
            else if (i == 13)
                removeGodClassIndex(ml_datasetCollection);
            showMenu();
            System.out.println("Riscegli o clicca 0 per uscire");
            i = scanner.nextInt();
        }
    }

    /**
     * Questo metodo stampa il menù
     */
    public static void showMenu() {
        System.out.println("Scegli l'operazione");

        System.out.println("1. Mostra test flaky");
        System.out.println("2. Mostra test non flaky");
        System.out.println("3. Mostra i test flaky che sono dipendenti da altre classi");
        System.out.println("4. Mostra tutti i test con lines of code > 100");
        System.out.println("5. Mostra, per ogni test, solo loc, cbo e isFlaky");
        System.out.println("6. Mostra la media di lines of code tra tutti i test");
        System.out.println("7. Mostra la media di lines of code tra tutti i test flaky");
        System.out.println("8. Mostra i massimi loc registrati per i test flaky e per i non flaky");
        System.out.println("9. Inserisci un nuovo elemento con campi nameProject, loc e cbo");
        System.out.println("10. Ricerca un elemento tramite nameProject");
        System.out.println("11. Ordinamento per loc crescente in pagine da 10");
        System.out.println("12. Rendi godClass un indice e lancia una covered query");
        System.out.println("13. Rimuovi godClass dagli indici");
    }

    /**
     * Questo metodo descrive se i risultati vanno in pagine da 10 test ciascuna o no
     * @return 1 se l'utente vuole le pagine da 10, 0 se non le vuole
     */
    public static int limit() {
        System.out.println("Vuoi un limite di 10 documenti di output? (1/0)");
        Scanner in = new Scanner(System.in);
        return in.nextInt();
    }

    /**
     * Questo metodo effettua la stampa dei documenti risultanti dalla find in modo paginato o no
     * @param mongoCollection collezione ml_dataset
     * @param filter il filtro della query
     */
    public static void showFilter(MongoCollection<Document> mongoCollection, Bson filter) {
        int flag = limit();

        if (flag != 0) {
            int skip = 0;
            for (int i = 1; i == 1; i = (new Scanner(System.in)).nextInt()) {
                for (Document document : mongoCollection.find(filter).skip(skip).limit(10))
                    System.out.println(document);
                skip += 10;
                System.out.println("Scrivi 1 per i dieci successivi, 0 per uscire");
            }
        }else
            for (Document document : mongoCollection.find(filter))
                System.out.println(document);
    }

    /**
     * Questo metodo mostra tutti i test flaky
     * @param mongoCollection collezione ml_dataset
     */
    public static void showFlakyTests(MongoCollection<Document> mongoCollection) {
        Bson condition = new Document("$eq",1);
        Bson filter = new Document("isFlaky",condition);

        showFilter(mongoCollection,filter);
    }

    /**
     * Questo metodo mostra tutti i test non flaky
     * @param mongoCollection collezione ml_dataset
     */
    public static void showNonFlakyTests(MongoCollection<Document> mongoCollection) {
        Bson condition = new Document("$eq",0);
        Bson filter = new Document("isFlaky",condition);

        showFilter(mongoCollection,filter);
    }

    /**
     * Questo metodo mostra tutti i test che sono flaky e con dipendenze verso altre classi
     * @param mongoCollection collezione ml_dataset
     */
    public static void showDependentFlakyTests(MongoCollection<Document> mongoCollection) {
        //filter1: cbo > 0
        Bson condition1 = new Document("$gt",0);
        Bson filter1 = new Document("cbo",condition1);

        //filter2: isFlaky = 1
        Bson condition2 = new Document("$eq",1);
        Bson filter2 = new Document("isFlaky",condition2);

        //or filter
        Bson filter = Filters.and(filter1,filter2);

        showFilter(mongoCollection,filter);
    }

    /**
     * Questo metodo mostra tutti i test con loc > 100
     * @param mongoCollection collezione ml_dataset
     */
    public static void showTestLocGT100(MongoCollection<Document> mongoCollection) {
        Bson condition = new Document("$gt",100);
        Bson filter = new Document("loc",condition);

        showFilter(mongoCollection,filter);
    }

    /**
     * Questo metodo mostra, per ogni test del dataset, solo loc, cbo ed isFlaky
     * @param mongoCollection collezione ml_dataset
     */
    public static void showLocCboFlakyTests(MongoCollection<Document> mongoCollection) {
        Bson projection = Projections.fields(Projections.include("loc","cbo","isFlaky"),
                Projections.exclude("_id"));

        int flag = limit();

        if (flag != 0) {
            int skip = 0;
            for (int i = 1; i == 1; i = (new Scanner(System.in)).nextInt()) {
                for (Document document : mongoCollection.find().projection(projection).skip(skip).limit(10))
                    System.out.println(document);
                skip += 10;
                System.out.println("Scrivi 1 per i dieci successivi, 0 per uscire");
            }
        }else
            for (Document document : mongoCollection.find().projection(projection))
                System.out.println(document);
    }

    /**
     * Questo metodo mostra la media di loc tra tutti i test del dataset
     * @param mongoCollection collezione ml_dataset
     */
    public static void showTestsLocAverage(MongoCollection<Document> mongoCollection) {
        for (Document document : mongoCollection.aggregate(Arrays.asList(
                Aggregates.group("all_tests",Accumulators.avg("loc_avg","$loc"))
        ))) {
            System.out.println(document);
        }
    }

    /**
     * Questo metodo mostra la media di loc tra tutti i test flaky del dataset
     * @param mongoCollection collezione ml_dataset
     */
    public static void showFlakyTestsLocAverage(MongoCollection<Document> mongoCollection){
        for (Document document : mongoCollection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("isFlaky", 1)),
                Aggregates.group("flaky_tests",Accumulators.avg("loc_avg","$loc"))
        ))) {
            System.out.println(document);
        }
    }

    /**
     * Questo metodo raggruppa i test del dataset in flaky e non flaky e,
     * per entrambi i gruppi, mostra il numero massimo registrato per la feature loc
     * @param mongoCollection collezione ml_dataset
     */
    public static void showMaxLocs(MongoCollection<Document> mongoCollection) {
        System.out.println("_id <=> isFlaky");
        for (Document document : mongoCollection.aggregate(Arrays.asList(
                Aggregates.group("$isFlaky",Accumulators.avg("loc_max","$loc"))
        ))) {
            System.out.println(document);
        }
    }

    /**
     * Questo metodo inserisce un nuovo test all'interno del DB con nameProject, loc e cbo
     * @param mongoCollection collezione ml_dataset
     */
    public static void insertTest(MongoCollection<Document> mongoCollection) {
        //input
        Scanner in = new Scanner(System.in);

        //output
        System.out.println("Inserisci un nameProject (string)");
        String nameProject = in.next("[A-Za-z0-9]*");
        System.out.println("Inserisci un loc (double)");
        double loc = in.nextDouble();
        System.out.println("Inserisci un cbo (double)");
        double cbo = in.nextDouble();

        //creo il nuovo documento
        Document document = new Document()
                .append("nameProject",nameProject)
                .append("loc",loc)
                .append("cbo",cbo);

        //aggiungo alla collezione
        mongoCollection.insertOne(document);

        System.out.println("Documento inserito con successo");
    }

    /**
     * Questo metodo recupera un documento sulla base del suo nameProject
     * @param mongoCollection collezione ml_dataset
     */
    public static void retrieveyNameProject(MongoCollection<Document> mongoCollection) {
        Scanner in = new Scanner(System.in);
        System.out.println("Inserisci nameProject");
        String nameProject = in.next("[A-Za-z0-9]*");
        Bson condition = new Document("$eq",nameProject);
        Bson filter = new Document("nameProject",condition);

        showFilter(mongoCollection,filter);
    }

    /**
     * Questo metodo restituisce la lista dei test ordinata secondo il parametro loc in pagine da 10
     * @param mongoCollection collezione ml_dataset
     */
    public static void sortByLoc(MongoCollection<Document> mongoCollection) {
        int skip = 0;
        for (int i = 1; i == 1; i = (new Scanner(System.in)).nextInt()) {
            for (Document document : mongoCollection.find().sort(ascending("loc")).skip(skip).limit(10))
                System.out.println(document);
            skip += 10;
            System.out.println("Scrivi 1 per i dieci successivi, 0 per uscire");
        }
    }

    /**
     * Questo metodo rende godCLass un indice
     * @param mongoCollection collezione ml_dataset
     */
    public static void makeGodClassIndex(MongoCollection<Document> mongoCollection) {
        String ascendingIndex = mongoCollection.createIndex(Indexes.ascending("godClass"));

        System.out.println(String.format("Index creato: %s",ascendingIndex));

        godClassCoveredQuery(mongoCollection);
    }

    /**
     * Questo metodo rimuove godClass come indice
     * @param mongoCollection collezione ml_dataset
     */
    private static void removeGodClassIndex(MongoCollection<Document> mongoCollection) {
        mongoCollection.dropIndex(Indexes.ascending("godClass"));

        System.out.println("Indice godClass rimosso");
    }

    /**
     * Questo metodo effettua una covered query sull'indice godClass
     * @param mongoCollection collezione ml_dataset
     */
    private static void godClassCoveredQuery(MongoCollection<Document> mongoCollection) {
        Bson filter = Filters.and(Filters.eq("godClass",0));
        Bson sort = Sorts.ascending("godClass");
        Bson projection = Projections.fields(Projections.include("godClass","isFlaky"),
                Projections.exclude("_id"));

        int skip = 0;
        for (int i = 1; i == 1; i = (new Scanner(System.in)).nextInt()) {
            for (Document document : mongoCollection.find(filter).sort(sort).projection(projection).skip(skip).limit(10))
                System.out.println(document);
            skip += 10;
            System.out.println("Scrivi 1 per i dieci successivi, 0 per uscire");
        }
    }

}
