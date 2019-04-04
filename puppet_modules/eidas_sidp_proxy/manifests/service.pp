# service.pp
class eidas_sidp_proxy::service inherits eidas_sidp_proxy {

  include platform

  if ($platform::deploy_spring_boot) {
    service { $eidas_sidp_proxy::service_name:
      ensure => running,
      enable => true,
    }
  }
}

