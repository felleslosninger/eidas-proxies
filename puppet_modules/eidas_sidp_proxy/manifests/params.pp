#params.pp
class eidas_sidp_proxy::params {
  $java_home                              = hiera('platform::java_home')
  $eventlog_jms_url                       = hiera('platform::jms_url')
  $eventlog_jms_queuename                 = hiera('idporten_logwriter::jms_queueName')
  $saml_proxy_idp_entity_name             = hiera('idporten_opensso::idp_entity_id')
  $eidas_node_url                         = hiera('eidas_sidp_proxy::eidas_node_url')
  $proxy_auth_url                         = hiera('idporten_opensso::eidas_module_saml_destination')
  $mf_gateway_url                         = hiera('mf::mf_gateway_url', 'http://localhost/')
  $mf_gateway_username                    = 'user'
  $mf_gateway_password                    = 'password'
  $eidas_sidp_proxy_url                   = hiera('eidas_sidp_proxy::url')
  $mf_gateway_timeout                     = 10000
  $mf_gateway_retry_count                 = 2
  $log_level                              = 'WARN'
  $iso_country_mapper_file                = 'isoCountryMapping.json'
  $available_countries                    = { 1 => { countryCode => 'CE', countryName => 'Demo country CE', attributes => '' } }
  $auditlog_dir                           = '/var/log/eidas-sidp-proxy/audit/'
  $auditlog_file                          = 'audit.log'
  $fileconfig_read_period                 = 60000
  $fileconfig_countries_attributes        = '/etc/opt/eidas-sidp-proxy/countriesAttributes.json'
  $eidas_sidp_proxy_dummy_enabled         = false
  $saml_instant_issue_time_to_live        = 300
  $saml_instant_issue_time_skew           = 60
  $saml_ip_address_validation             = false
  $saml_requester                         = 'http://eid.difi.no.sid'
  $saml_responder                         = 'http://eid.difi.no.sid'
  $saml_check_certificate_validity_period = false
  $saml_disallow_self_signed_certificate  = false
  $idporten_keystore_password             = 'password'
  $idporten_keystore_privatekey_password  = 'password'
  $idporten_keystore_location             = 'idPortenKeystore.jks'
  $idporten_keystore_alias                = 'selfsigned'
  $idporten_keystore_type                 = 'jks'
  $mf_test_users                          = [ { eidas_identifier => '19890605.CE/NO/05061989',
    d_number                                                     => '05068907693' } ]
  $key_store                              = { storetype          => 'JKS',
    password                                                     => 'changeit',
    name                                                         => 'NO_Eidas_Sign_JKS.jks',
    issuer                                                       => 'CN=Norwegian Eidas Test Certificate, OU=SSU, O=Agency for Public Management and eGovernment (Difi), C=NO, EMAILADDRESS=idporten@difi.no',
    serialNumber                                                 => '55A8DB49',
    keyPassword                                                  => 'changeit', }
  $trust_store                            = { storetype => 'JKS',
    password                                            => 'changeit',
    name                                                => 'NO_Eidas_Trust_JKS.jks', }
  $security_providers                     = ''
  $server_port                            = 10008
  $log_root                               = '/var/log/'

  $application                            = 'eidas-sidp-proxy'
  $service_name                           = 'eidas-sidp-proxy'
  $context                                = 'eidas-sidp-proxy'
  $artifact_id                            = 'eidas-sidp-proxy'
  $install_dir                            = '/opt/'
  $config_dir                             = '/etc/opt/'
  $tomcat_tmp_dir                         = '/opt/eidas-sidp-proxy/tmp'
  $health_show_details                    = 'always'
  $server_tomcat_max_threads              = 200
  $server_tomcat_min_spare_threads        = 10
}
