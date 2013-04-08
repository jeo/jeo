package jeo

import java.io.File
import org.jeo.feature.Feature

object Conversions {

    implicit def strToFile(str:String) = new File(str)

    //implicit def featureToList(f:Feature) = f.list()
    //implicit def featureToMap(f:Feature) = f.map()
}