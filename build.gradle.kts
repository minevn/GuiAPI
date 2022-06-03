group = "net.minevn"
version = "1.0-SNAPSHOT"

plugins {
	id("java")
}

repositories {
	mavenCentral()
	mavenLocal()
	maven {
		setUrl("http://pack.minevn.net/repo/")
		isAllowInsecureProtocol = true
	}

}

dependencies {
	compileOnly("minevn.depend:paper:1.12.2-b1619")
	implementation("org.jetbrains:annotations:23.0.0")
}

tasks {
	val jarName = "GuiAPI"
	var originalName = ""
	val path = project.properties["shadowPath"]

	jar {
		originalName = archiveFileName.get()
	}

	register("customCopy") {
		dependsOn(jar)

		doLast {
			if (path != null) {
				println("Copying $originalName to $path")
				val to = File("$path/$originalName")
				val rename = File("$path/$jarName.jar")
				File(project.projectDir, "build/libs/$originalName").copyTo(to, true)
				if (rename.exists()) rename.delete()
				to.renameTo(rename)
				println("Copied")
			}
		}
	}


	assemble {
		dependsOn(get("customCopy"))
	}
}
