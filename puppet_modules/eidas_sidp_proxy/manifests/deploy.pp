#deploy.pp
class eidas_sidp_proxy::deploy inherits eidas_sidp_proxy {

  include 'difilib'

  difilib::spring_boot_deploy { $eidas_sidp_proxy::application:
    package       => 'no.difi.eidas',
    artifact      => $eidas_sidp_proxy::artifact_id,
    service_name  => $eidas_sidp_proxy::service_name,
    install_dir   => "${eidas_sidp_proxy::install_dir}${eidas_sidp_proxy::application}",
    artifact_type => "war",
  }
}