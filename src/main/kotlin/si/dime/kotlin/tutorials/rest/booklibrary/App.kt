package si.dime.kotlin.tutorials.rest.booklibrary

import com.beust.klaxon.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson

import jdk.nashorn.internal.parser.JSONParser
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.RequestBody

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.nio.ByteBuffer

import java.util.*
import java.awt.image.BufferedImage
import java.io.*
import javax.imageio.ImageIO

import java.io.ByteArrayInputStream
import sun.misc.BASE64Decoder
import java.util.Base64
import java.nio.file.Files

@SpringBootApplication
open class App {
}

data class Dirreccion(var origen: String, var destiny: String)
@RestController
@RequestMapping(value = "/tarea1")
class Tarea1 {
    fun GetDirections(orig :String, dest :String): Any?{
        var direcLin: String="https://maps.googleapis.com/maps/api/directions/json?origin="
        val direcKey :String= "AIzaSyB-LSaU_1XzGQmZIJRzdHNOjxEzO3ZWIM4"
        direcLin = direcLin.plus(orig.plus("&destination=").plus(dest.plus("&key=").plus(direcKey)))
        val (request, respo, resu) = direcLin.httpGet().responseString()
        if(respo.httpStatusCode<200 || respo.httpStatusCode>=300){
            val myParser: Parser = Parser()
            val myBuilder: StringBuilder = StringBuilder("Error, vuelva a intentarlo")
            val myJson: JsonObject = myParser.parse(myBuilder) as JsonObject

            return myJson
        }else{
            val myParser: Parser = Parser()
            var myBuilder: StringBuilder = StringBuilder(resu.component1())
            var myJson2: JsonObject = myParser.parse(myBuilder) as JsonObject
            val myVar= JsonArray(myJson2.toMap().get("routes"))["legs"]["steps"]["end_location"]
            myVar.add(0,JsonArray(myJson2.toMap().get("routes"))["legs"]["steps"]["start_location"][0])
            var myLink = "{\n\"ruta\":[\n"
            for (any in myVar) {
                myLink = myLink.plus("{\n\"lat\": ")
                myLink = myLink.plus(JsonArray(any).get("lat")[0].toString()).plus(",").plus("\n")
                myLink = myLink.plus("\"lon\": ")
                if (any == myVar[myVar.lastIndex]) {
                    myLink = myLink.plus(JsonArray(any).get("lng")[0].toString()).plus("\n").plus("}\n")
                }
                else{
                    myLink = myLink.plus(JsonArray(any).get("lng")[0].toString()).plus("\n").plus("},\n")
                }
            }
            myLink = myLink.plus("]\n}")
            myBuilder = StringBuilder(myLink)
            myJson2 = myParser.parse(myBuilder) as JsonObject
            return myJson2
        }
    }
    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun helloSpringBoot(@RequestBody directions: Dirreccion)= GetDirections(directions.origen, directions.destiny)
    }

data class Lugares(var origen: String)
@RestController
@RequestMapping(value = "/tarea2")
class Tarea2 {
    fun GetLatLong(orig: String): Any?{
        var direcLin: String="https://maps.googleapis.com/maps/api/geocode/json?address="
        val direcKey :String= "AIzaSyAseBk911mUaaKXC8JyUUZDPWD5XaV8gdA"
        direcLin= direcLin.plus("?address=").plus((orig).plus("&key=").plus(direcKey))
        val (request, respo, result) = direcLin.httpGet().responseString() // result is Result<String, FuelError>
        if(respo.httpStatusCode<200 || respo.httpStatusCode>=300){
            val myParser: Parser = Parser()
            val myBuild: StringBuilder = StringBuilder("Error, vuelva a intentarlo")
            val myJson: JsonObject = myParser.parse(myBuild) as JsonObject

            return respo.httpStatusCode
        }else{
            val myParser: Parser = Parser()
            val myBuild: StringBuilder = StringBuilder(result.component1())
            val myJson: JsonObject = myParser.parse(myBuild) as JsonObject
            return JsonArray(myJson.toMap().get("results"))["geometry"]["location"]
        }
    }
    fun GetRestaurants(orig :String): Any?{
        val coordi = JsonArray(GetLatLong(orig))
        val latCoor = coordi["lat"][0]
        val lonCoor = coordi["lng"][0]
        var siteLin: String= "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
        val siteKey: String= "AIzaSyCfj-2YgW8o8Q4Oy68p8mc9DBs_fntH_nk"
        siteLin = siteLin.plus(latCoor.toString()).plus(",").plus(lonCoor.toString()).plus("&radius=500&type=restaurant&key=").plus(siteKey)
        val (request, respo, result) = siteLin.httpGet().responseString()
        if(respo.httpStatusCode<200 || respo.httpStatusCode>=300){
            return "Error"
        }else{
            val myParser: Parser = Parser()
            var myBuild: StringBuilder = StringBuilder(result.component1())
            var myJson: JsonObject = myParser.parse(myBuild) as JsonObject
            val restauArray = JsonArray(myJson.toMap().get("results"))
            val restauNames = restauArray["name"]
            val restauLats = restauArray["geometry"]["location"]
            var myLink = "{\n\"Restaurantes\": [\n"
            var cont=0
            for (restauNames in restaurantNames) {
                myLink = myLink.plus("{\n\"nombre\":")
                myLink = myLink.plus("\"").plus(restauNames).plus("\",").plus("\n")
                myLink = myLink.plus("\"lat\":").plus(restauLats.get("lat")[cont]).plus(",\n")
                myLink = myLink.plus("\"lon\":").plus(restauLats.get("lng")[cont]).plus("\n")
                cont+=1
                if(restauNames == restaurantNames[restaurantNames.lastIndex]){
                    myLink = myLink.plus("}\n")
                }else{
                    myLink = myLink.plus("},\n")
                }
            }
            myLink = myLink.plus("]\n}")
            myBuild = StringBuilder(myLink)
            myJson = myParser.parse(myBuild) as JsonObject
            return myJson
        }
    }
    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun helloSpringBoot(@RequestBody place: Lugares) = GetRestaurants(place.origen)
}

@RestController
class Tarea3 {
    @RequestMapping("/tarea3", method = arrayOf(RequestMethod.POST))
    fun imgGray(@RequestBody image: img) : Any? {

        val myData = img.data
        val img2: BufferedImage?
        
        val imgByte: ByteArray
        val decoder = BASE64Decoder()

        imgByte = decoder.decodeBuffer(myData)
        val bis = ByteArrayInputStream(imgByte)
        img2 = ImageIO.read(bis)

        for(x in 0..img2.width - 1){
            for(y in 0..img2.height - 1){
                val rgb = img2.getRGB(x, y)

                val red = (rgb shr 16) and 0xFF
                val green = (rgb shr 8) and 0xFF
                val blue = (rgb and 0xFF)

                val graySca = ((red + green + blue) / 3)
                val gray = graySca shl 16 or (graySca shl 8) or graySca

                img2.setRGB(x, y, gray)
            }
        }

        val salFile = File("img_gs.bmp")
        ImageIO.write(img2, "bmp", salFile)
        val myFile = File("img_gs.bmp")

        val byte : ByteArray = Files.readAllBytes(myFile.toPath())
        val encoder = Base64.getEncoder().encodeToString(byte)
        val myLink = "{\"nombre\":\"" + img.nombre + "\",\"data\":\""+encoder+"\"}"

        val myParser : Parser = Parser()
        val myBuild: StringBuilder = StringBuilder(myLink)
        val myJson: JsonObject = myParser.parse(myBuild) as JsonObject

        return myJson
    }
}

class image {
    lateinit var nombre: String
    lateinit var data: String
    lateinit var tamaño: tamaño
}

class tamaño{
    var ancho: Int = 0
    var alto: Int = 0
}

@RestController
class Tarea4 {
    @RequestMapping("/tarea4", method = arrayOf(RequestMethod.POST))
    fun imgRedu(@RequestBody req: image) : Any? {

        val data = req.data
		val img2: BufferedImage?
        val imgByte: ByteArray

        val decoder = BASE64Decoder()
        imgByte = decoder.decodeBuffer(data)
        val agmh = ByteArrayInputStream(imgByte)
        
        img2 = ImageIO.read(agmh)
        val anchura = img2.width
        val altura = img2.height

        val facAncho = (anchura.toFloat()/(req.tamaño.ancho).toFloat())
        val facAltura = (altura.toFloat()/(req.tamaño.alto).toFloat())
        val newAncho= (anchura/facAncho).toInt()
		val newAltura = (altura/facAltura).toInt()

		val newIMG: BufferedImage = BufferedImage(newAncho, newAltura, BufferedImage.TYPE_INT_RGB)
        for(x in 0 unitl newAncho){
            for(y in 0 unitl newAltura){
                val myPix = img2.getRGB((x * facAncho).toInt(), (y * facAltura).toInt())
                newIMG.setRGB(x, y, myPix)
            }
        }

        val salFile = File("img_rdx.bmp")
        ImageIO.write(newIMG, "bmp", salFile)
        val myFile = File("imgRedu.bmp")

        val myByte : ByteArray = Files.readAllBytes(myFile.toPath())
        val encoder = Base64.getEncoder().encodeToString(myByte)
        val myLink = "{\"nombre\":\"" + req.nombre + "\",\"data\":\""+encoder+"\"}"

        val myParser : Parser = Parser()
        val myBuild: StringBuilder = StringBuilder(myLink)
        val myJson: JsonObject = myParser.parse(myBuild) as JsonObject

        return myJson
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(App::class.java, *args)
}