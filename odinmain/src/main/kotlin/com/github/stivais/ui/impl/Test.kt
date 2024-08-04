package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.constraints.copies
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.elements.impl.Grid
import me.odinmain.features.impl.render.ClickGUI.`gray 38`


fun basic() = UI {

//    Column(at(Center, Center), 5.px, 5.px).scope {
////        block(constrain(100.px, 100.px, 200.px, 200.px), Color.RED)
//        text("wtf", pos = at(x = Center), size = 20.px)
//        text("a", pos = at(x = Center), size = 20.px)
//        text("123", pos = at(x = Center), size = 20.px)
//        text("bcdef", pos = at(x = Center), size = 20.px)
//        text("ghjijk", pos = at(x = Center), size = 20.px)
//    }

   Grid(copies()).scope {
       repeat(4) {
           group(size(50.percent, 50.percent)) {
               block(size(80.percent, 50.percent), `gray 38`)
           }
       }
   }

}