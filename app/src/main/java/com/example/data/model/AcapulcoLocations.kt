package com.example.data.model

enum class MacroZona(val codigo: String, val titulo: String, val descripcion: String) {
    ZONA_A("ZONA_A", "Zona A: Costera / Cima / Farallón", "Costera, Farallón, Magallanes, Progreso, Centro y Caleta"),
    ZONA_B("ZONA_B", "Zona B: Diamante / Puerto Marqués", "Blvd. de las Naciones, Puerto Marqués, Revolcadero, Barra Vieja"),
    ZONA_C("ZONA_C", "Zona C: Pie de la Cuesta / Poniente", "Pie de la Cuesta, Mozimba, San Isidro, Zona Poniente"),
    ZONA_D("ZONA_D", "Zona D: Renacimiento / Zapata", "Cd. Renacimiento, Col. Emiliano Zapata, Vacacional, El Quemado")
}

data class ZonaAcapulco(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val puntosReferencia: List<String>,
    val zonaTier: Int, // 1: Costera/Centro, 2: Dorada/Costa Azul, 3: Tradicional/Caleta, 4: Diamante/Aeropuerto, 5: Renacimiento/Zapata, 6: Pie de la Cuesta
    val macroZona: MacroZona,
    val lat: Double,
    val lng: Double
)

data class TarifaDesglose(
    val base: Double,
    val plusTipoServicio: Double,
    val recargoZonaLejana: Double,
    val macroZonaOrigen: MacroZona,
    val macroZonaDestino: MacroZona,
    val cruceMacroZonas: Boolean,
    val total: Double
)

object AcapulcoLocations {
    val ZONAS = listOf(
        ZonaAcapulco(
            id = "costera",
            nombre = "Costera Miguel Alemán",
            descripcion = "Zona hotelera, Galerías Diana, La Diana Cazadora, Papagayo",
            puntosReferencia = listOf("Galerías Diana", "Parque Papagayo", "Diana Cazadora", "Hotel Fiesta Americana", "Asta Bandera"),
            zonaTier = 1,
            macroZona = MacroZona.ZONA_A,
            lat = 16.8531,
            lng = -99.8705
        ),
        ZonaAcapulco(
            id = "costa_azul",
            nombre = "Costa Azul & Club Deportivo",
            descripcion = "Centro de Convenciones, Walmart Costera, Club de Golf",
            puntosReferencia = listOf("Walmart Costera", "Centro de Convenciones", "Iglesia Costa Azul", "Chedraui Selecto"),
            zonaTier = 2,
            macroZona = MacroZona.ZONA_A,
            lat = 16.8480,
            lng = -99.8510
        ),
        ZonaAcapulco(
            id = "magallanes",
            nombre = "Magallanes & Galerías Acapulco",
            descripcion = "Zona comercial y bancaria, Gran Plaza / Galerías Acapulco",
            puntosReferencia = listOf("Galerías Acapulco", "Hospital Magallanes", "Torre Médica", "Av. Wilfrido Massieu"),
            zonaTier = 1,
            macroZona = MacroZona.ZONA_A,
            lat = 16.8580,
            lng = -99.8760
        ),
        ZonaAcapulco(
            id = "progreso",
            nombre = "Colonia Progreso & Cuauhtémoc",
            descripcion = "Clínica IMSS 9, Calle Sonora, Av. Niños Héroes, Mercado Progreso",
            puntosReferencia = listOf("Clínica IMSS 9", "Mercado de la Progreso", "Calle Sonora", "Estadio UAGro"),
            zonaTier = 2,
            macroZona = MacroZona.ZONA_A,
            lat = 16.8640,
            lng = -99.8890
        ),
        ZonaAcapulco(
            id = "caleta",
            nombre = "Tradicional / Caleta & Playas",
            descripcion = "Zócalo de Acapulco, La Quebrada, Playas Caleta y Caletilla",
            puntosReferencia = listOf("La Quebrada", "Zócalo Centro", "Playa Caletilla", "Fuerte de San Diego", "Sinfonía del Mar"),
            zonaTier = 3,
            macroZona = MacroZona.ZONA_A,
            lat = 16.8340,
            lng = -99.9070
        ),
        ZonaAcapulco(
            id = "diamante",
            nombre = "Zona Diamante & Blvd. de las Naciones",
            descripcion = "La Isla Shopping Village, Princess Mundo Imperial, Arena GNP Seguros",
            puntosReferencia = listOf("La Isla Shopping", "Princess Mundo Imperial", "Arena GNP", "Chedraui Diamante", "Playa Revolcadero"),
            zonaTier = 4,
            macroZona = MacroZona.ZONA_B,
            lat = 16.7820,
            lng = -99.7890
        ),
        ZonaAcapulco(
            id = "barra_vieja",
            nombre = "Barra Vieja & Aeropuerto",
            descripcion = "Aeropuerto Internacional de Acapulco, Pescado a la Talla, Plan de los Amates",
            puntosReferencia = listOf("Aeropuerto de Acapulco", "Restaurantes Barra Vieja", "Plan de los Amates"),
            zonaTier = 5,
            macroZona = MacroZona.ZONA_B,
            lat = 16.7580,
            lng = -99.7530
        ),
        ZonaAcapulco(
            id = "renacimiento",
            nombre = "Cd. Renacimiento & Col. Zapata",
            descripcion = "Hospital General El Quemado, Eje Central, Tecnológico de Acapulco",
            puntosReferencia = listOf("Hospital General El Quemado", "Instituto Tecnológico", "Plaza Caracol", "Eje Central Vicente Guerrero"),
            zonaTier = 5,
            macroZona = MacroZona.ZONA_D,
            lat = 16.8920,
            lng = -99.8320
        ),
        ZonaAcapulco(
            id = "pie_cuesta",
            nombre = "Pie de la Cuesta",
            descripcion = "Laguna de Coyuca, Puesta del sol, Carretera Nacional",
            puntosReferencia = listOf("Laguna de Coyuca", "Playa Pie de la Cuesta", "Base Aérea"),
            zonaTier = 5,
            macroZona = MacroZona.ZONA_C,
            lat = 16.9050,
            lng = -99.9920
        ),
        ZonaAcapulco(
            id = "puerto_marques",
            nombre = "Puerto Marqués & Pichilingue",
            descripcion = "Bahía de Puerto Marqués, Glorieta Puerto Marqués, Majahua",
            puntosReferencia = listOf("Bahía Puerto Marqués", "Playa Majahua", "Glorieta de Puerto Marqués"),
            zonaTier = 3,
            macroZona = MacroZona.ZONA_B,
            lat = 16.8040,
            lng = -99.8250
        )
    )

    fun getZona(nombreZona: String): ZonaAcapulco {
        return ZONAS.find { it.nombre.equals(nombreZona, ignoreCase = true) }
            ?: ZONAS.firstOrNull { nombreZona.contains(it.id, ignoreCase = true) }
            ?: ZONAS[0]
    }

    fun getMacroZona(nombreZona: String): MacroZona {
        return getZona(nombreZona).macroZona
    }

    fun getCoordenadasParaZona(nombreZona: String): Pair<Double, Double> {
        val zona = getZona(nombreZona)
        return Pair(zona.lat, zona.lng)
    }

    /**
     * Calcula la distancia en kilómetros entre dos coordenadas en Acapulco usando la fórmula Haversine.
     */
    fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radio de la tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    /**
     * Geofencing Acapulco: Si un envío cruza entre macro-zonas lejanas (ej. Zona A Costera <-> Zona B Diamante,
     * o Zona C Pie de la Cuesta <-> Zona B Diamante), se aplica un recargo por zona lejana (+ $25 MXN)
     * para proteger la ganancia del repartidor y evitar rechazo de viajes largos.
     */
    fun calcularRecargoZonaLejana(zonaOrigen: String, zonaDestino: String): Double {
        val macroOrigen = getMacroZona(zonaOrigen)
        val macroDestino = getMacroZona(zonaDestino)
        return if (macroOrigen != macroDestino) 25.0 else 0.0
    }

    fun calcularTarifaDesglose(zonaOrigen: String, zonaDestino: String, tipo: String): TarifaDesglose {
        val origen = getZona(zonaOrigen)
        val destino = getZona(zonaDestino)

        val diffTier = kotlin.math.abs(origen.zonaTier - destino.zonaTier)
        val tarifaBase = when {
            diffTier == 0 -> 45.0 // Misma zona o adyacente inmediata
            diffTier == 1 -> 60.0
            diffTier == 2 -> 80.0
            diffTier == 3 -> 110.0
            diffTier == 4 -> 135.0
            else -> 155.0
        }

        val plusTipoServicio = if (tipo == "COMPRAS" || tipo == "FARMACIA") 15.0 else 0.0
        val recargoLejana = calcularRecargoZonaLejana(zonaOrigen, zonaDestino)
        val total = tarifaBase + plusTipoServicio + recargoLejana

        return TarifaDesglose(
            base = tarifaBase,
            plusTipoServicio = plusTipoServicio,
            recargoZonaLejana = recargoLejana,
            macroZonaOrigen = origen.macroZona,
            macroZonaDestino = destino.macroZona,
            cruceMacroZonas = origen.macroZona != destino.macroZona,
            total = total
        )
    }

    fun calcularTarifa(zonaOrigen: String, zonaDestino: String, tipo: String): Double {
        return calcularTarifaDesglose(zonaOrigen, zonaDestino, tipo).total
    }
}
