import { useState } from 'react'
import type { Endereco, Pessoa } from '../domain/pessoa.types'
import { useAtualizarMeusDados } from './useMeusDados'

const TELEFONE_VALIDO = /^\(?\d{2}\)?\s?9?\d{4}-?\d{4}$/
const CEP_VALIDO = /^\d{5}-?\d{3}$/
const UF_VALIDA = /^[A-Za-z]{2}$/
const INSTAGRAM_VALIDO = /^@?[A-Za-z0-9._]{1,30}$/

/**
 * Mesmo cálculo de dígito verificador do backend (`CpfValidator`). Duplicar a regra aqui é o que
 * evita uma ida ao servidor para dizer o que dá para ver no formulário; o backend continua sendo
 * quem decide.
 */
export function cpfValido(cpf: string): boolean {
  const digitos = cpf.replace(/\D/g, '')
  if (digitos.length !== 11 || new Set(digitos).size === 1) return false

  const dv = (base: string) => {
    const soma = [...base].reduce((total, digito, i) => total + Number(digito) * (base.length + 1 - i), 0)
    const resto = 11 - (soma % 11)
    return String(resto >= 10 ? 0 : resto)
  }

  const base = digitos.slice(0, 9)
  const primeiro = dv(base)
  return digitos === base + primeiro + dv(base + primeiro)
}

type CamposEndereco = { [K in keyof Endereco]: string }

interface Campos extends CamposEndereco {
  cpf: string
  dataNascimento: string
  telefone: string
  instagram: string
  alergias: string
  restricaoAlimentar: string
  medicacaoUsoContinuo: string
  contatoEmergenciaNome: string
  contatoEmergenciaTelefone: string
  contatoEmergenciaParentesco: string
}

function camposDe(pessoa: Pessoa | undefined): Campos {
  return {
    cpf: pessoa?.cpf ?? '',
    dataNascimento: pessoa?.dataNascimento ?? '',
    telefone: pessoa?.telefone ?? '',
    instagram: pessoa?.instagram ?? '',
    cep: pessoa?.endereco.cep ?? '',
    logradouro: pessoa?.endereco.logradouro ?? '',
    numero: pessoa?.endereco.numero ?? '',
    complemento: pessoa?.endereco.complemento ?? '',
    bairro: pessoa?.endereco.bairro ?? '',
    cidade: pessoa?.endereco.cidade ?? '',
    uf: pessoa?.endereco.uf ?? '',
    alergias: pessoa?.alergias ?? '',
    restricaoAlimentar: pessoa?.restricaoAlimentar ?? '',
    medicacaoUsoContinuo: pessoa?.medicacaoUsoContinuo ?? '',
    contatoEmergenciaNome: pessoa?.contatoEmergenciaNome ?? '',
    contatoEmergenciaTelefone: pessoa?.contatoEmergenciaTelefone ?? '',
    contatoEmergenciaParentesco: pessoa?.contatoEmergenciaParentesco ?? '',
  }
}

/** Primeiro erro encontrado, na ordem em que os campos aparecem na tela. */
function primeiroErro(campos: Campos): string | undefined {
  if (!cpfValido(campos.cpf)) return 'Informe um CPF válido.'
  if (!campos.dataNascimento) return 'Informe a data de nascimento.'
  if (!TELEFONE_VALIDO.test(campos.telefone.trim())) return 'Informe um telefone válido, com DDD.'
  if (campos.instagram.trim() && !INSTAGRAM_VALIDO.test(campos.instagram.trim()))
    return 'Informe um perfil do Instagram válido.'
  if (!CEP_VALIDO.test(campos.cep.trim())) return 'Informe um CEP válido.'
  if (!campos.logradouro.trim()) return 'Informe o logradouro.'
  if (!campos.numero.trim()) return 'Informe o número.'
  if (!campos.bairro.trim()) return 'Informe o bairro.'
  if (!campos.cidade.trim()) return 'Informe a cidade.'
  if (!UF_VALIDA.test(campos.uf.trim())) return 'Informe a UF com duas letras.'
  return undefined
}

/**
 * Edição dos próprios dados. O formulário só entra em modo de edição quando a pessoa pede — assim
 * a tela abre como leitura do próprio cadastro, que é o uso mais comum, e não como um formulário.
 */
export function useMeusDadosForm(pessoa: Pessoa | undefined) {
  const atualizar = useAtualizarMeusDados()
  const [editando, setEditando] = useState(false)
  const [campos, setCampos] = useState<Campos>(camposDe(pessoa))
  const [erro, setErro] = useState<string | undefined>(undefined)
  const [salvo, setSalvo] = useState(false)

  function editar() {
    setCampos(camposDe(pessoa))
    setErro(undefined)
    setSalvo(false)
    setEditando(true)
  }

  function cancelar() {
    setEditando(false)
    setErro(undefined)
  }

  function alterar(campo: keyof Campos, valor: string) {
    setCampos((atual) => ({ ...atual, [campo]: valor }))
    setSalvo(false)
  }

  async function salvar() {
    const invalido = primeiroErro(campos)
    if (invalido) {
      setErro(invalido)
      return
    }
    // Campo opcional em branco volta como nulo, não como string vazia: "não informado" e "informado
    // como nada" são coisas diferentes para quem lê a ficha depois.
    const vazioComoNulo = (valor: string) => (valor.trim() ? valor.trim() : null)
    try {
      await atualizar.mutateAsync({
        cpf: campos.cpf.trim(),
        dataNascimento: campos.dataNascimento,
        telefone: campos.telefone.trim(),
        instagram: vazioComoNulo(campos.instagram),
        endereco: {
          cep: campos.cep.trim(),
          logradouro: campos.logradouro.trim(),
          numero: campos.numero.trim(),
          complemento: vazioComoNulo(campos.complemento),
          bairro: campos.bairro.trim(),
          cidade: campos.cidade.trim(),
          uf: campos.uf.trim().toUpperCase(),
        },
        alergias: vazioComoNulo(campos.alergias),
        restricaoAlimentar: vazioComoNulo(campos.restricaoAlimentar),
        medicacaoUsoContinuo: vazioComoNulo(campos.medicacaoUsoContinuo),
        contatoEmergenciaNome: vazioComoNulo(campos.contatoEmergenciaNome),
        contatoEmergenciaTelefone: vazioComoNulo(campos.contatoEmergenciaTelefone),
        contatoEmergenciaParentesco: vazioComoNulo(campos.contatoEmergenciaParentesco),
      })
      setEditando(false)
      setSalvo(true)
    } catch (falha) {
      // 409 é o CPF de outra pessoa (RN-PESSOA-01) — dizer só "tente novamente" faria a pessoa
      // repetir o mesmo CPF indefinidamente.
      const status = (falha as { response?: { status?: number } }).response?.status
      setErro(
        status === 409
          ? 'Este CPF já pertence a outra pessoa do cadastro. Confira o número ou fale com a sua paróquia.'
          : 'Não foi possível salvar. Tente novamente.',
      )
    }
  }

  return {
    editando,
    campos,
    erro,
    salvo,
    isSalvando: atualizar.isPending,
    editar,
    cancelar,
    alterar,
    salvar,
  }
}
