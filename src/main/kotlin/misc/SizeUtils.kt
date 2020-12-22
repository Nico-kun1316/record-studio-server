package misc

val Int.kB: Long
    get() = this * 1024L

val Int.MB: Long
    get() = this.kB * 1024L

val Int.GB: Long
    get() = this.MB * 1024L

val Long.kB: Long
    get() = this * 1024L

val Long.MB: Long
    get() = this.kB * 1024L

val Long.GB: Long
    get() = this.MB * 1024L
