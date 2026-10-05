package com.codewithmohamed.quranwordbyword

object ReaderRules {
    @JvmStatic fun clamp(page: Int,count: Int): Int {
        require(count>0) {"Empty PDF"}; return page.coerceIn(1,count)
    }
    @JvmStatic fun parsePage(text: String,count: Int): Int {
        val page=text.trim().toIntOrNull() ?: throw IllegalArgumentException("Enter a whole page number")
        require(page in 1..count) {"Enter a page from 1 to $count"};return page
    }
    @JvmStatic fun validJuzMap(starts: IntArray,count: Int): Boolean {
        if(starts.size!=30) return false
        var previous=0
        for(start in starts) {
            if(start==0) continue
            if(start<=previous || start>count) return false
            previous=start
        }
        return true
    }
    @JvmStatic fun renderSize(width: Int,height: Int,screenWidth: Int): IntArray {
        val scale=minOf(2400.0/width,maxOf(1600.0,screenWidth*2.5)/width,
            kotlin.math.sqrt(6_000_000.0/(width.toDouble()*height)))
        return intArrayOf((width*scale).toInt().coerceAtLeast(1),(height*scale).toInt().coerceAtLeast(1))
    }
}
