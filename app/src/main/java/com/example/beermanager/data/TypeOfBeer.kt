package com.example.beermanager.data

enum class TypeOfBeer{
    NONALCOHOLIC{
        override fun toString()= "NON-ALCOHOLIC"
    }
    ,TEN{
        override fun toString()= "10°"
    }
    , ELEVEN{
        override fun toString()= "11°"
    }
    , TWELVE{
        override fun toString()= "12°"
    }
    , THIRTEEN{
        override fun toString()= "13°"
    };
    abstract override fun toString(): String
}
