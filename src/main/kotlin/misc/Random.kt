package misc

import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

val RNG by lazy { SecureRandom.getInstanceStrong().asKotlinRandom() }

fun Random.nextShort(from: Short, to: Short): Short = nextInt(from.toInt(), to.toInt()).toShort()

fun Random.nextShort(to: Short): Short = nextShort(0, to)

fun Random.nextShort(): Short = nextShort(Short.MIN_VALUE, Short.MAX_VALUE)

fun Random.nextShort(range: IntRange) = nextShort(range.first.toShort(), range.last.toShort())
