/*** BEGIN META {
 "name" : "smart_queue",
 "comment" : "showcase smart_queue project",
 "parameters" : ['NODE', 'TYPE'],
 "core": "1.609",
 "authors" : [{ name : "livelace" }]
 } END META**/

@Grab('org.yaml:snakeyaml:1.17')

import org.yaml.snakeyaml.Yaml

def BASE_PATH = "/var/jenkins_home/showcase/smart-queue/config"
def NODE_FILE = "${BASE_PATH}/node.yaml"

def RESULT

def parser = new Yaml()
def nodes = parser.load((NODE_FILE as File).text)
def node = nodes.find { it.name == NODE }

switch (TYPE) {
    case "cpu":
        RESULT = []

        node ? node.hardware.cpu.times { RESULT.add(it + 1) } : null
        break

    case "gpu":
        RESULT = [:]

        node ? node.hardware.gpu.each { RESULT[it.id] = it.name } : null
        break

    case "node":
        RESULT = []

        nodes.each { RESULT.add(it.name) }
        break

    case "stat":
        try {
            def active_lease_file = "${BASE_PATH}/${NODE}_active.yaml"
            def wait_lease_file = "${BASE_PATH}/${NODE}_wait.yaml"

            def activeLease = parser.load((active_lease_file as File).text)
            def waitLease = parser.load((wait_lease_file as File).text)

            def active_build_total   = 0
            def active_cpu_total     = 0
            def active_gpu_total     = 0
            def active_gpu_stat      = node.hardware.gpu.collectEntries { [(it.id): 0] }
            def active_gpu_stat_name = node.hardware.gpu.collectEntries { [(it.id): it.name] }

            def wait_build_total     = 0
            def wait_cpu_total       = 0
            def wait_gpu_total       = 0
            def wait_gpu_stat        = node.hardware.gpu.collectEntries { [(it.id): 0] }
            def wait_gpu_stat_name   = node.hardware.gpu.collectEntries { [(it.id): it.name] }

            activeLease.each { lease ->
                active_build_total += 1
                active_cpu_total += lease.hardware.cpu
                active_gpu_total += lease.hardware.gpu.size()
                lease.hardware.gpu.each { active_gpu_stat[it.id] += 1}
            }

            waitLease.each { lease ->
                wait_build_total += 1
                wait_cpu_total += lease.hardware.cpu
                wait_gpu_total += lease.hardware.gpu.size()
                lease.hardware.gpu.each { wait_gpu_stat[it.id] += 1}
            }

            def ACTIVE_GPU_STAT = ""
            active_gpu_stat.each { index, amount ->
                ACTIVE_GPU_STAT += """${active_gpu_stat_name[index]}: <font color="red">${amount}</font> """
            }

            def WAIT_GPU_STAT = ""
            wait_gpu_stat.each { index, amount ->
                WAIT_GPU_STAT += """${wait_gpu_stat_name[index]}: <font color="blue">${amount}</font> """
            }

            RESULT = """
            <b>active build:</b> <font color="red">${active_build_total}</font>, <b>active cpu:</b> <font color="red">${active_cpu_total}</font>, <b>active gpu:</b> <font color="red">${active_gpu_total}</font> <br> ${ACTIVE_GPU_STAT} <br><br>
            <b>wait build:</b> <font color="blue">${wait_build_total}</font>, <b>wait cpu:</b> <font color="blue">${wait_cpu_total}</font>, <b>wait gpu:</b> <font color="blue">${wait_gpu_total}</font> <br> ${WAIT_GPU_STAT} <br>
            """
        } catch (ignored) {
            RESULT = "---"
        }

        break
}

if (RESULT) {
    return RESULT
} else {
    return ["---"]
}
