package com.petshop.api.customer.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum DogBreed {

    // ===== Vira-lata =====
    MIXED_BREED("Vira-lata"),

    // ===== Raças pequenas / companhia =====
    AFFENPINSCHER("Affenpinscher"),
    BICHON_FRISE("Bichon Frisé"),
    BOLOGNESE("Bolonhês"),
    BOSTON_TERRIER("Boston Terrier"),
    BRUSSELS_GRIFFON("Griffon Belga"),
    CAVALIER_KING_CHARLES_SPANIEL("Cavalier King Charles Spaniel"),
    CHIHUAHUA("Chihuahua"),
    CHINESE_CRESTED("Crestado Chinês"),
    COTON_DE_TULEAR("Coton de Tuléar"),
    DACHSHUND("Dachshund (Salsicha)"),
    FRENCH_BULLDOG("Bulldog Francês"),
    HAVANESE("Havanês"),
    ITALIAN_GREYHOUND("Pequeno Galgo Italiano"),
    JACK_RUSSELL_TERRIER("Jack Russell Terrier"),
    JAPANESE_CHIN("Chin Japonês"),
    KING_CHARLES_SPANIEL("King Charles Spaniel"),
    LHASA_APSO("Lhasa Apso"),
    MALTESE("Maltês"),
    MINIATURE_PINSCHER("Pinscher Miniatura"),
    PAPILLON("Papillon"),
    PEKINGESE("Pequinês"),
    PINSCHER("Pinscher"),
    POMERANIAN("Lulu da Pomerânia"),
    POODLE("Poodle"),
    POODLE_TOY("Poodle Toy"),
    PUG("Pug"),
    RAT_TERRIER("Rat Terrier"),
    SHIBA_INU("Shiba Inu"),
    SHIH_TZU("Shih Tzu"),
    SILKY_TERRIER("Silky Terrier"),
    WEST_HIGHLAND_WHITE_TERRIER("West Highland White Terrier"),
    YORKSHIRE_TERRIER("Yorkshire"),

    // ===== Raças médias =====
    AMERICAN_STAFFORDSHIRE_TERRIER("American Staffordshire Terrier"),
    AUSTRALIAN_CATTLE_DOG("Boiadeiro Australiano"),
    AUSTRALIAN_SHEPHERD("Pastor Australiano"),
    BASENJI("Basenji"),
    BASSET_HOUND("Basset Hound"),
    BEAGLE("Beagle"),
    BORDER_COLLIE("Border Collie"),
    BORDER_TERRIER("Border Terrier"),
    BRAZILIAN_TERRIER("Terrier Brasileiro"),
    BRITTANY("Braco Bretão"),
    BULL_TERRIER("Bull Terrier"),
    BULLDOG("Buldogue Inglês"),
    CANAAN_DOG("Cão de Canaã"),
    CHOW_CHOW("Chow Chow"),
    COCKER_SPANIEL("Cocker Spaniel"),
    COLLIE("Collie"),
    DALMATIAN("Dálmata"),
    ENGLISH_SETTER("Setter Inglês"),
    ENGLISH_SPRINGER_SPANIEL("Springer Spaniel Inglês"),
    FINNISH_SPITZ("Spitz Finlandês"),
    FOX_TERRIER("Fox Terrier"),
    GERMAN_SPITZ("Spitz Alemão"),
    KEESHOND("Keeshond"),
    KERRY_BLUE_TERRIER("Kerry Blue Terrier"),
    LAGOTTO_ROMAGNOLO("Lagotto Romagnolo"),
    PORTUGUESE_PODENGO("Podengo Português"),
    PORTUGUESE_WATER_DOG("Cão de Água Português"),
    PUMI("Pumi"),
    SAMOYED("Samoieda"),
    SCHNAUZER("Schnauzer"),
    SCHNAUZER_GIGANTE("Schnauzer Gigante"),
    SHAR_PEI("Shar Pei"),
    SHETLAND_SHEEPDOG("Pastor de Shetland"),
    SIBERIAN_HUSKY("Husky Siberiano"),
    STAFFORDSHIRE_BULL_TERRIER("Staffordshire Bull Terrier"),
    VIZSLA("Vizsla"),
    WELSH_CORGI_CARDIGAN("Welsh Corgi Cardigan"),
    WELSH_CORGI_PEMBROKE("Welsh Corgi Pembroke"),
    WHIPPET("Whippet"),
    WIRE_FOX_TERRIER("Fox Terrier de Pelo Duro"),

    // ===== Raças grandes =====
    AIREDALE_TERRIER("Airedale Terrier"),
    AKITA("Akita"),
    ALASKAN_MALAMUTE("Malamute do Alasca"),
    BEARDED_COLLIE("Bearded Collie"),
    BELGIAN_MALINOIS("Pastor Belga Malinois"),
    BELGIAN_SHEPHERD("Pastor Belga"),
    BLOODHOUND("Bloodhound"),
    BOXER("Boxer"),
    BRAZILIAN_MASTIFF("Fila Brasileiro"),
    BULLMASTIFF("Bullmastiff"),
    CHESAPEAKE_BAY_RETRIEVER("Chesapeake Bay Retriever"),
    DOBERMAN("Doberman"),
    DOGO_ARGENTINO("Dogo Argentino"),
    DOGUE_DE_BORDEAUX("Dogue de Bordeaux"),
    FLAT_COATED_RETRIEVER("Flat-Coated Retriever"),
    GERMAN_SHEPHERD("Pastor Alemão"),
    GERMAN_SHORTHAIRED_POINTER("Braco Alemão de Pelo Curto"),
    GOLDEN_RETRIEVER("Golden Retriever"),
    GORDON_SETTER("Setter Gordon"),
    GREATER_SWISS_MOUNTAIN_DOG("Boiadeiro Suíço"),
    IRISH_SETTER("Setter Irlandês"),
    IRISH_WOLFHOUND("Lébrel Irlandês"),
    LABRADOR_RETRIEVER("Labrador"),
    OLD_ENGLISH_SHEEPDOG("Bobtail"),
    POINTER("Pointer"),
    RHODESIAN_RIDGEBACK("Rhodesian Ridgeback"),
    ROTTWEILER("Rottweiler"),
    SAINT_BERNARD("São Bernardo"),
    STANDARD_POODLE("Poodle Standard"),
    WEIMARANER("Weimaraner"),

    // ===== Raças gigantes =====
    ANATOLIAN_SHEPHERD("Pastor de Anatólia"),
    BERNESE_MOUNTAIN_DOG("Boiadeiro de Berna"),
    BOERBOEL("Boerboel"),
    BORZOI("Borzoi"),
    CANE_CORSO("Cane Corso"),
    CAUCASIAN_SHEPHERD("Pastor do Cáucaso"),
    ENGLISH_MASTIFF("Mastiff Inglês"),
    GREAT_DANE("Dogue Alemão"),
    GREAT_PYRENEES("Pastor dos Pirineus"),
    LEONBERGER("Leonberger"),
    MASTIFF_NAPOLITANO("Mastiff Napolitano"),
    NEWFOUNDLAND("Terra Nova"),
    TIBETAN_MASTIFF("Mastim do Tibete"),

    // ===== Nordicos / spitz =====
    AMERICAN_ESKIMO_DOG("Spitz Americano"),
    JAPANESE_SPITZ("Spitz Japonês"),
    NORWEGIAN_ELKHOUND("Elkhound Norueguês"),

    // ===== Terriers adicionais =====
    CAIRN_TERRIER("Cairn Terrier"),
    DANDIE_DINMONT_TERRIER("Dandie Dinmont Terrier"),
    LAKELAND_TERRIER("Lakeland Terrier"),
    MANCHESTER_TERRIER("Manchester Terrier"),
    NORFOLK_TERRIER("Norfolk Terrier"),
    NORWICH_TERRIER("Norwich Terrier"),
    SCOTTISH_TERRIER("Scottish Terrier"),
    SEALYHAM_TERRIER("Sealyham Terrier"),
    SKYE_TERRIER("Skye Terrier"),
    WELSH_TERRIER("Welsh Terrier"),

    // ===== Sabujos / farejadores =====
    AFGHAN_HOUND("Galgo Afegão"),
    AMERICAN_FOXHOUND("Foxhound Americano"),
    BASSET_FAUVE_DE_BRETAGNE("Basset Fauve de Bretagne"),
    BLUETICK_COONHOUND("Coonhound"),
    ENGLISH_FOXHOUND("Foxhound Inglês"),
    GREYHOUND("Galgo"),
    HARRIER("Harrier"),
    OTTERHOUND("Otterhound"),
    SALUKI("Saluki"),
    SCOTTISH_DEERHOUND("Deerhound Escocês");

    private final String portugueseName;

    DogBreed(String portugueseName) {
        this.portugueseName = portugueseName;
    }

    @JsonValue
    public String getPortugueseName() {
        return portugueseName;
    }
}